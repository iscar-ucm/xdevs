import logging
from typing import Dict, List, NoReturn, Optional
from xdevs.transducers import Transducer
try:
    from sqlalchemy import create_engine, text, Column, Float, Integer, MetaData, String, Table
    from sqlalchemy.sql.type_api import TypeEngine
    _dependencies_ok = True
except ModuleNotFoundError:
    _dependencies_ok = False


class SQLTransducer(Transducer):

    supported_data_types: Dict[type, TypeEngine]

    def __init__(self, **kwargs):
        """
        xDEVS transducer for relational databases. It uses the SQLAlchemy driver.
        :param str url: URL of the database. It must follow the scheme required by the SQLAlchemy driver.
        :param bool echo: If set to True, transducer will log the interactions with the database. Defaults to False.
        :param bool pool_pre_ping: If set to True, transducer ensures that connections are open before
        executing any operation. If needed, connections will be re-opened. Defaults to True.
        :param int pool_recycle: If set to non -1, number of seconds between connection recycling. Defaults to -1.
        :param int string_length: Maximum number of characters per string entries on the database. Defaults to 128.
        """
        if not _dependencies_ok:
            raise ImportError('Dependencies are not imported')
        self.string_length: int = kwargs.get('string_length', 128)
        super().__init__(**kwargs)
        self.activate_remove_special_numbers()

        url: str = kwargs.get('url')
        if url is None:
            raise AttributeError('You must specify a database URL.')
        echo: bool = kwargs.get('echo', False)
        pool_pre_ping: bool = kwargs.get('pool_pre_ping', True)
        pool_recycle: int = kwargs.get('pool_recycle', -1)
        self.engine = create_engine(url, echo=echo, pool_pre_ping=pool_pre_ping, pool_recycle=pool_recycle)

        self.state_table: Optional[Table] = None
        self.event_table: Optional[Table] = None

    def create_known_data_types_map(self) -> Dict[type, TypeEngine]:
        return {str: String(self.string_length), int: Integer, float: Float}

    def initialize(self) -> NoReturn:
        metadata = MetaData()
        if self.target_components:
            table_name: str = self.transducer_id + '_states'
            columns = [Column('id', self.supported_data_types[int], primary_key=True, autoincrement=True),
                       Column('sim_time', self.supported_data_types[float], nullable=False),
                       Column('model_name', self.supported_data_types[str], nullable=False)]
            self.state_table = self.create_table(table_name, columns, self.state_mapper, metadata)
        if self.target_ports:
            table_name: str = self.transducer_id + '_events'
            columns = [Column('id', self.supported_data_types[int], primary_key=True, autoincrement=True),
                       Column('sim_time', self.supported_data_types[float], nullable=False),
                       Column('model_name', self.supported_data_types[str], nullable=False),
                       Column('port_name', self.supported_data_types[str], nullable=False)]
            self.event_table = self.create_table(table_name, columns, self.event_mapper, metadata)
        metadata.create_all(self.engine)

    def bulk_data(self, sim_time: float) -> NoReturn:
        with self.engine.connect() as conn:
            for insertion in self._iterate_state_inserts(sim_time):
                conn.execute(self.state_table.insert().values(**insertion))
            for insertion in self._iterate_event_inserts(sim_time):
                conn.execute(self.event_table.insert().values(**insertion))

    def exit(self) -> NoReturn:
        self.engine.dispose()

    def create_table(self, table_name: str, columns: List[Column],
                     columns_mapper: Dict[str, tuple], metadata: MetaData) -> Table:
        # 1. Remove table if already exists
        with self.engine.connect() as conn:
            conn.execute('DROP TABLE IF EXISTS {}'.format(table_name))

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
        return Table(table_name, metadata, *columns)
