from setuptools import setup, find_packages

setup(name='xdevs',
      version='2.2.1',
      description='xDEVS M&S framework',
      url='https://github.com/dacya/xdevs',
      author='Kevin Henares, Román Cárdenas',
      author_email='khenares@ucm.es, r.cardenas@upm.es',
      packages=find_packages(exclude=['xdevs.tests']),
      entry_points={
          'xdevs.plugins.transducers': [
              'csv = xdevs.plugins.transducers.csv_transducer:CSVTransducer',
              'sql = xdevs.plugins.transducers.sql_transducer:SQLTransducer',
              'elasticsearch = xdevs.plugins.transducers.elasticsearch_transducer:ElasticsearchTransducer',
          ],
          'xdevs.plugins.wrappers': [
              'pypdevs = xdevs.plugins.wrappers.pypdevs:PyPDEVSWrapper'
          ]},
      extras_require={
          'sql': ['sqlalchemy==1.3.22'],
          'elasticsearch': ['elasticsearch==7.10.1'],
      },
      zip_safe=False)
