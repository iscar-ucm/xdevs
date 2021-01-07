import logging
from typing import Any, Dict, List, NoReturn, Optional
from ...transducers import Transducer
try:
    from sqlalchemy import create_engine, text, Column, Float, Integer, MetaData, String, Table
    _dependencies_ok = True
except ModuleNotFoundError:
    _dependencies_ok = False


class SQLTransducer(Transducer):
    def __init__(self, **kwargs):
        """xDEVS transducer for relational databases. It uses the SQLAlchemy driver"""
        if not _dependencies_ok:
            raise ImportError('Dependencies are not imported')
        super().__init__(**kwargs)
        self.activate_remove_special_numbers()

        url: str = kwargs.get('url')
        echo: bool = kwargs.get('echo', False)
        self.engine = create_engine(url, echo=echo)
        self.string_length: int = kwargs.get('string_length', 128)
        self.state_table: Optional[Table] = None
        self.event_table: Optional[Table] = None
        self.supported_data_types = {str: String(self.string_length), int: Integer, float: Float}

    def _is_data_type_unknown(self, field_type) -> bool:
        return field_type not in self.supported_data_types

    def initialize(self) -> NoReturn:
        if self.target_components:
            table_name: str = self.transducer_id + '_states'
            columns = [Column('id', Integer, primary_key=True, autoincrement=True),
                       Column('sim_time', Float, nullable=False),
                       Column('model_name', String(self.string_length), nullable=False)]
            self.state_table = self.create_table(table_name, columns, self.state_mapper)
        if self.target_ports:
            table_name: str = self.transducer_id + '_events'
            columns = [Column('id', Integer, primary_key=True, autoincrement=True),
                       Column('sim_time', Float, nullable=False),
                       Column('model_name', String(self.string_length), nullable=False),
                       Column('port_name', String(self.string_length), nullable=False)]
            self.event_table = self.create_table(table_name, columns, self.event_mapper)

    def bulk_data(self, state_inserts: List[Dict[str, Any]], event_inserts: List[Dict[str, Any]]) -> NoReturn:
        conn = self.engine.connect()
        try:
            if state_inserts:
                conn.execute(self.state_table.insert(), state_inserts)
            if event_inserts:
                conn.execute(self.event_table.insert(), event_inserts)
        finally:
            conn.close()

    def create_table(self, table_name: str, columns: List[Column], columns_mapper: Dict[str, tuple]) -> Table:
        # 1. When table exist, we ask the user if he/she wants to overwrite it
        metadata = MetaData(self.engine, reflect=True)
        table = metadata.tables.get(table_name)
        if table is not None:
            print('SQL transducer {}: table {} already exists on {}.'.format(self.transducer_id, table_name,
                                                                             self.engine.url))
            if input('Do you want to overwrite it? [Y/n] >').lower() in ['n', 'no']:
                raise FileExistsError('table already exists on DB and user does not want to overwrite it')
            conn = self.engine.connect()
            conn.execute(text('DROP TABLE IF EXISTS {};'.format(table_name)))
            conn.close()

        # 2. Deduce the columns of the table and their corresponding data type
        for state_field, (field_type, field_getter) in columns_mapper.items():
            column_type = self.supported_data_types.get(field_type, None)
            if column_type is None:
                column_type = self.supported_data_types[str]
                # TODO move this warning to parent class
                logging.warning('SQL transducer {} will cast type of column {} to string'.format(self.transducer_id,
                                                                                                 state_field))
            columns.append(Column(state_field, column_type))
        # 3. Create the table
        metadata = MetaData()
        table = Table(table_name, metadata, *columns)
        table.create(self.engine)
        return table
