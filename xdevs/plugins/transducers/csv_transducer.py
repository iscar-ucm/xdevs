import csv
import os
from typing import Any, List, NoReturn, Iterable, Type
from ...transducers import Transducer


class CSVTransducer(Transducer):
    def __init__(self, **kwargs):
        """
        xDEVS-to-CSV file transducer.
        :param delimiter: delimiter used by the CSV transducer. By default, it is set to ",".
        :param output_dir: directory that will contain the output CSV files. By default, it is set to "./"
        """
        super().__init__(**kwargs)
        self.delimiter: str = kwargs.get('delimiter', ',')
        output_dir: str = kwargs.get('output_dir', '.')
        self.state_filename = os.path.join(output_dir, self.transducer_id + '_states.csv')
        self.event_filename = os.path.join(output_dir, self.transducer_id + '_events.csv')

        self.state_header: List[str] = [self.sim_time_id]
        self.event_header: List[str] = [self.sim_time_id]
        if self.include_names:
            self.state_header.append(self.model_name_id)
            self.event_header.extend((self.model_name_id, self.port_name_id))

        self.state_csv_file = None
        self.event_csv_file = None
        self.state_csv_writer = None
        self.event_csv_writer = None

    def create_known_data_types_map(self) -> Iterable[Type[Any]]:
        return [str, int, float, bool]

    def initialize(self) -> NoReturn:
        if self.target_components:
            self.state_header.extend(self.state_mapper)
            self.state_csv_file, self.state_csv_writer = self._create_csv_file(self.state_filename, self.state_header)
        if self.target_ports:
            self.event_header.extend(self.event_mapper)
            self.event_csv_file, self.event_csv_writer = self._create_csv_file(self.event_filename, self.event_header)

    def exit(self):
        if self.state_csv_file:
            self.state_csv_file.close()
        if self.event_csv_file:
            self.event_csv_file.close()

    def bulk_data(self, sim_time: float):
        for state_insert in self._iterate_state_inserts(sim_time):
            self.state_csv_writer.writerow([state_insert[field] for field in self.state_header])

        for event_insert in self._iterate_event_inserts(sim_time):
            self.event_csv_writer.writerow([event_insert[field] for field in self.event_header])

    def _create_csv_file(self, filename: str, header: List[str]):
        # 1. If output file already exist, we ask the use if he/she wants to overwrite it.
        # if os.path.exists(filename):
        #     print('Transducer output file {} already exists.'.format(filename))
        #     if input('Do you want to overwrite it? [Y/n] >').lower() in ['n', 'no']:
        #         raise FileExistsError('File already exists and user does not want to overwrite it')

        # 2. If directory that will contain the file does not exist, we create it.
        os.makedirs(os.path.dirname(filename), exist_ok=True)

        # 3. Create output CSV file and write the header row.
        csv_file = open(filename, 'w')
        writer = csv.writer(csv_file, delimiter=self.delimiter)
        writer.writerow(header)

        return csv_file, writer
