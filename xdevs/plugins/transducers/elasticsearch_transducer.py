import logging
from typing import Dict, NoReturn
from ...transducers import Transducer
try:
    from elasticsearch import Elasticsearch
    _dependencies_ok = True
except ModuleNotFoundError:
    _dependencies_ok = False


class ElasticsearchTransducer(Transducer):

    supported_data_types: Dict[type, str]

    def __init__(self, **kwargs):
        """
        xDEVS transducer for Elasticsearch databases.
        :param str url: URL of one of the nodes of the Elasticsearch database.
        :param int number_of_shards: Number of shards to be used for each Elasticsearch index. Defaults to 1.
        :param int number_of_replicas: Number of replicas to be used for each Elasticsearch index. Defaults to 1.
        """
        if not _dependencies_ok:
            raise ImportError('Dependencies are not imported')
        super().__init__(**kwargs)

        self.url: str = kwargs.get('url')
        if self.url is None:
            raise AttributeError('You must specify an Elasticsearch URL.')
        self.number_of_shards: int = kwargs.get('number_of_shards', 1)
        self.number_of_replicas: int = kwargs.get('number_of_replicas', 1)
        self.activate_remove_special_numbers()

        self.es = None

    def create_known_data_types_map(self) -> Dict[type, str]:
        return {str: 'text', int: 'integer', float: 'double', bool: 'boolean'}

    def initialize(self) -> NoReturn:
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
        if self.target_components or self.target_ports:
            self.es = Elasticsearch([self.url])

    def bulk_data(self, sim_time: float) -> NoReturn:
        for insertion in self._iterate_state_inserts(sim_time):
            self.es.index(index=self.transducer_id + '_states', body=insertion)
        for insertion in self._iterate_event_inserts(sim_time):
            self.es.index(index=self.transducer_id + '_events', body=insertion)

    def exit(self) -> NoReturn:
        pass

    def create_index(self, index_name: str, field_properties: Dict[str, Dict[str, str]], others: Dict[str, tuple]):
        es = Elasticsearch([self.url])
        # 1. When index exist, we overwrite it
        if es.indices.exists(index=index_name):
            logging.warning('ES transducer {}: index {} already exists on {}. '
                            'It will be overwritten.'.format(self.transducer_id, index_name, self.url))
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
