import os
import random
import string

__author__ = 'Giannis Giannakopoulos'


def get_random_file_name(size=20):
    """
    This function returns a random file name
    """
    picks = string.lowercase[:26] + "1"+"2"+"3"+"4"+"5"+"6"+"7"+"8"+"9"+"0"+string.uppercase[:26]
    return ''.join(random.choice(picks) for _ in range(1, size + 1))


def generate_ssh_key_pair(keys_prefix, keys_dir="/tmp/keys"):
    """
    This function generates a new SSH key pair in the specified location.
    The key generation is conducted using the os's command ssh-keygen.
    """
    os.system("mkdir -p "+keys_dir)
    os.system("ssh-keygen -f "+keys_dir+"/"+keys_prefix+" -N '' -C 'keys@deployer' -b 2048")
    return {'public':keys_dir+"/"+keys_prefix+".pub", 'private':keys_dir+"/"+keys_prefix}
