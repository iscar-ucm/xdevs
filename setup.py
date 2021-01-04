from setuptools import setup

setup(name='xdevs',
      version='2.1',
      description='xDEVS M&S framework',
      url='https://github.com/dacya/xdevs',
      author='Kevin Henares, Román Cárdenas',
      author_email='khenares@ucm.es, r.cardenas@upm.es',
      packages=['xdevs'],
      entry_points={
          'xdevs.plugins.transducers': [
              'csv = xdevs.plugins.transducers.csv_transducer:CSVTransducer',
              'sql = xdevs.plugins.transducers.sql_transducer:SQLTransducer',
          ]},
      extra_require={
          'sql': ['sqlalchemy==1.3.22']
      },
      zip_safe=False)
