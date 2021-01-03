import csv
import errno
import os
from typing import Any, IO, List, NoReturn, Optional
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
        output_dir: str = kwargs.get('output_dir', './')
        output_dir = output_dir if output_dir[-1] == '/' else output_dir + '/'
        self.state_filename = output_dir + self.transducer_id + '_states.csv'
        self.event_filename = output_dir + self.transducer_id + '_events.csv'
        self.state_file: Optional[IO] = None
        self.event_file: Optional[IO] = None
        self.state_writer = None
        self.event_writer = None

    def initialize_transducer(self) -> NoReturn:
        if self.target_components:
            header: List[str] = ['time', 'name', 'phase', 'sigma']
            header.extend(self.state_mapper)
            self.create_csv_file(self.state_filename, header)
        if self.target_ports:
            header: List[str] = ['time', 'model_name', 'port_name']
            header.extend(self.event_mapper)
            self.create_csv_file(self.event_filename, header)

    def set_up_transducer(self) -> NoReturn:
        if self.target_components:
            self.state_file = open(self.state_filename, 'a', newline='')
            self.state_writer = csv.writer(self.state_file, delimiter=self.delimiter)
        if self.target_ports:
            self.event_file = open(self.event_filename, 'a', newline='')
            self.event_writer = csv.writer(self.event_file, delimiter=self.delimiter)

    def tear_down_transducer(self) -> NoReturn:
        if self.state_file is not None and not self.state_file.closed:
            self.state_writer = None
            self.state_file.close()
        if self.event_file is not None and not self.event_file.closed:
            self.event_writer = None
            self.event_file.close()

    def bulk_state_data(self, time: float, name: str, phase: str, sigma: float, **kwargs) -> NoReturn:
        row: List[Any] = [time, name, phase, sigma]
        row.extend(kwargs.values())
        self.state_writer.writerow(row)

    def bulk_event_data(self, time: float, model_name: str, port_name: str, **kwargs) -> NoReturn:
        row: List[Any] = [time, model_name, port_name]
        row.extend(kwargs.values())
        self.event_writer.writerow(row)

    def create_csv_file(self, filename: str, header: List[str]):
        # 1. If output file already exist, we ask the use if he/she wants to overwrite it.
        if os.path.exists(filename):
            print('Transducer output file {} already exists.'.format(filename))
            if input('Do you want to overwrite it? [Y/n] >').lower() in ['n', 'no']:
                raise FileExistsError('File already exists and user does not want to overwrite it')
        # 2. If directory that will contain the file does not exist, we create it.
        elif not os.path.exists(os.path.dirname(filename)):
            try:
                os.makedirs(os.path.dirname(filename))
            except OSError as exc:  # Guard against race condition
                if exc.errno != errno.EEXIST:
                    raise exc
        # 3. Create output CSV file and write the header row.
        with open(filename, 'w', newline='') as csv_file:
            writer = csv.writer(csv_file, delimiter=self.delimiter)
            writer.writerow(header)
