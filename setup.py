from setuptools import setup

setup(name='xdevs',
      version='1.2',
      description='xDEVS M&S framework',
      url='https://github.com/dacya/xdevs',
      author='Kevin Henares, Román Cárdenas',
      author_email='khenares@ucm.es, r.cardenas@upm.es',
      packages=['xdevs'],
      entry_points={
        'xdevs.plugins.transducers': [
              'dummy = xdevs.plugins.transducers.dummy_transducer:DummyTransducer',
              'broken = xdevs.plugins.transducers.broken_transducer:BrokenTransducer',
        ]},
      zip_safe=False)
