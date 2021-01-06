import logging
from typing import Any, Dict, List, NoReturn
from ...transducers import Transducer
try:
    from elasticsearch import Elasticsearch
    _dependencies_ok = True
except ModuleNotFoundError:
    _dependencies_ok = False


class ElasticsearchTransducer(Transducer):
    def __init__(self, **kwargs):
        """xDEVS transducer for Elasticsearch databases."""
        if not _dependencies_ok:
            raise ImportError('Dependencies are not imported')
        super().__init__(**kwargs)

        self.url: str = kwargs.get('url')
        self.number_of_shards: int = kwargs.get('number_of_shards', 1)
        self.number_of_replicas: int = kwargs.get('number_of_replicas', 1)

        self.supported_data_types = {str: 'text', int: 'integer', float: 'double', bool: 'boolean'}
        self.activate_remove_special_numbers()

    def is_data_type_unknown(self, field_type) -> bool:
        return field_type not in self.supported_data_types

    def initialize_transducer(self) -> NoReturn:
        if self.target_components:
            fields = {
                'sim_time': {'type': 'double'},
                'model_name': {'type': 'text'},
            }
            self.create_index(self.transducer_id + '_states', fields, self.state_mapper)
        if self.target_ports:
            fields = {
                'sim_time': {'type': 'double'},
                'model_name': {'type': 'text'},
                'port_name': {'type': 'text'},
            }
            self.create_index(self.transducer_id + '_events', fields, self.event_mapper)

    def bulk_data(self, state_inserts: List[Dict[str, Any]], event_inserts: List[Dict[str, Any]]) -> NoReturn:
        es = Elasticsearch([self.url])
        for inserts, target in [(state_inserts, self.transducer_id + '_states'),
                                (event_inserts, self.transducer_id + '_events')]:
            for insert in inserts:
                es.index(index=target, body=insert)

    def create_index(self, index_name: str, field_properties: Dict[str, Dict[str, str]], others: Dict[str, tuple]):
        es = Elasticsearch([self.url])
        # 1. When index exist, we ask the user if he/she wants to overwrite it
        if es.indices.exists(index=index_name):
            print('ES transducer {}: index {} already exists on {}.'.format(self.transducer_id, index_name, self.url))
            if input('Do you want to overwrite it? [Y/n] >').lower() in ['n', 'no']:
                raise FileExistsError('index already exists on DB and user does not want to overwrite it')
            else:
                es.indices.delete(index=index_name, ignore=[400, 404])
        # 2, Create field mapping
        for field_name, (field_type, field_getter) in others.items():
            es_type = self.supported_data_types.get(field_type, None)
            if es_type is None:
                es_type = self.supported_data_types[str]
                # TODO move this warning to parent class
                logging.warning('Elasticsearch transducer {} will cast field {} to string'.format(self.transducer_id,
                                                                                                  field_name))
            field_properties[field_name] = {'type': es_type}
        mapping = {
            'settings': {
                'number_of_shards': self.number_of_shards,
                'number_of_replicas': self.number_of_replicas,
            },
            'mappings': {
                'properties': field_properties
            },
        }
        # 3. Create index
        es.indices.create(index=index_name, body=mapping, ignore=400)
