import numpy as np
import matplotlib.pyplot as plt

# Create x and y NumPy arrays
x = np.arange(1, 11)
y = np.arange(1, 11)

# Create x and y axis from NumPy arrays
x_axis, y_axis = np.meshgrid(x, y)

# Create z = (x-5)^2 + (y-5)^2
z = ((x_axis - 5) ** 2) + ((y_axis - 5) ** 2)

# Create contour with different colors at each (x,y) location
plt.contourf(x_axis, y_axis, z, 20, alpha = 0.5, cmap = plt.get_cmap('jet'))

# Mark out the z's minimum value
plt.plot([5], [5], 'o', ms=12, markeredgewidth=3, color='orange')

# Mark the x axis
plt.xlim(1,10)
plt.xlabel(r'$X$',fontsize=16)

# Mark the y axis
plt.ylim(1,10)
plt.ylabel(r'$Y$',fontsize=16)

# Title the figure
plt.title('Michael_Kerl_Quiz1Contour', fontsize=16)

# Show Figure
plt.show()