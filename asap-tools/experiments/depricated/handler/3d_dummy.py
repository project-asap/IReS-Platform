#!/usr/bin/env python
__author__ = 'cmantas'
from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm
from matplotlib.ticker import LinearLocator, FormatStrFormatter
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = fig.gca(projection='3d')
X = np.arange(-5, 5, 0.25)
Y = np.arange(-5, 5, 0.25)
X, Y = np.meshgrid(X, Y)
R = np.sqrt(X**2 + Y**2)
Z = np.sin(R)
print Z
ax = fig.add_subplot(111, projection='3d')
a=[1,2,3]
b=[10,20,30]
c=[200,100,10]
ax.scatter(a,b,c)
plt.show()