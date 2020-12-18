from ...transducers import Transducer
try:
    import broken  # This will fail -> we have a flag to avoid
    _dependencies_ok = True
except ModuleNotFoundError:
    _dependencies_ok = False


class BrokenTransducer(Transducer):
    '''Broken transducer. It will be loaded automatically to the Transducers factory'''
    def __init__(self, **kwargs):
        if not _dependencies_ok:  # If we try to create a broken transducer, we will raise an exception
            raise ImportError('Dependencies are not imported')
        super().__init__(**kwargs)
