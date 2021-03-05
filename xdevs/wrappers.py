
from typing import Dict, NoReturn, Type
import pkg_resources
from xdevs.models import Atomic

class Wrappers:
    _plugins: Dict[str, Type[Atomic]] = {ep.name: ep.load()
                                         for ep in pkg_resources.iter_entry_points('xdevs.plugins.wrappers')}

    @staticmethod
    def add_plugin(name: str, plugin: Type[Atomic]) -> NoReturn:
        if name in Wrappers._plugins:
            raise ValueError('xDEVS wrapper plugin with name "{}" already exists'.format(name))
        Wrappers._plugins[name] = plugin

    @staticmethod
    def get_wrapper(name: str) -> Type[Atomic]:
        if name not in Wrappers._plugins:
            raise ValueError('xDEVS wrapper plugin with name "{}" not found'.format(name))
        return Wrappers._plugins[name]