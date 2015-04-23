from setuptools import setup, find_packages

setup(
    name="panic-deployer",
    version="0.1",
    packages=find_packages(),
    requires=['kamaki',
              'paramiko'],
    author='Giannis Giannakopoulos',
    author_email='ggian@cslab.ece.ntua.gr'
)