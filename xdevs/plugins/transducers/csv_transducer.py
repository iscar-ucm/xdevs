import csv
import errno
import os
from typing import Any, Dict, List, NoReturn
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

        self.state_header: List[str] = ['sim_time', 'model_name']
        self.event_header: List[str] = ['sim_time', 'model_name', 'port_name']

    def is_data_type_unknown(self, field_type) -> bool:
        return field_type in [str, int, float, bool]

    def initialize_transducer(self) -> NoReturn:
        if self.target_components:
            self.state_header.extend(self.state_mapper)
            self.create_csv_file(self.state_filename, self.state_header)
        if self.target_ports:
            self.event_header.extend(self.event_mapper)
            self.create_csv_file(self.event_filename, self.event_header)

    def bulk_data(self, state_inserts: List[Dict[str, Any]], event_inserts: List[Dict[str, Any]]) -> NoReturn:
        for (inserts, filename, header) in [(state_inserts, self.state_filename, self.state_header),
                                            (event_inserts, self.event_filename, self.event_header)]:
            if inserts:
                with open(filename, 'a', newline='') as csv_file:
                    writer = csv.writer(csv_file, delimiter=self.delimiter)
                    for insert in inserts:
                        writer.writerow([insert[field] for field in header])

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
