import numpy as np
import matplotlib.pyplot as plt

x_data = np.array([35., 38., 31., 20., 22., 25., 17., 60., 8., 60.])
y_data = 2 * x_data + 50 + 5 * np.random.random(10)

bb = np.arange(0, 100, 1)
ww = np.arange(-5, 5, 0.1)
Z = np.zeros((len(bb), len(ww)))

# Create landscape
for i in range(len(bb)):
  for j in range(len(ww)):
    b = bb[i]
    w = ww[j]
    Z[j][i] = 0
    for n in range(len(x_data)):
      Z[j][i] += (1/2) * (w * x_data[n] + b - y_data[n]) ** 2

# Initial values for Gradient Descent
b = 0
w = 0
lr = 0.00015 # 0.00015741
iterations = 15000 # 8500
b_history = [b]
w_history = [w]

# Run Gradient Descent process
for i in range(iterations):
  b_grad = 0.0
  w_grad = 0.0
  for n in range(len(x_data)):
    loss = w * x_data[n] + b - y_data[n]
    b_grad += loss
    w_grad += loss * x_data[n]
  b -= lr * b_grad
  w -= lr * w_grad
  b_history.append(b)
  w_history.append(w)

# Get final values used for testing
b_final = b_history[len(b_history) - 1]
w_final = w_history[len(w_history) - 1]

# Plot figure
plt.figure(figsize = (8, 7))

# Gradient Descent process
plt.subplot(211)
plt.title('Gradient Descent', fontweight = 'bold')
plt.xlim(0, 99)
plt.xlabel('b', fontstyle = 'italic')
plt.ylim(-5, 4.9)
plt.ylabel('w', fontstyle = 'italic', rotation = 'horizontal')
plt.contourf(bb, ww, Z, 50, alpha = 0.5, cmap = plt.get_cmap('jet'))
plt.plot(b_history, w_history, 'o-', ms = 3, lw = 1.5, color = 'black')
plt.plot([b_final], [w_final], 'o-', ms=3, color='orange')
plt.annotate("b = " + str(round(b_final, 2)) + ", w = " + str(round(w_final, 2)), xy = (b_final, w_final), xytext = (b_final + 1, w_final + 0.2))

# Prediction testing
plt.subplot(212)
plt.title('Prediction vs Actual', fontweight = 'bold')
plt.xlim(0, 80)
plt.xlabel('x', fontstyle = 'italic')
plt.ylim(0, 250)
plt.ylabel('y', fontstyle = 'italic', rotation = 'horizontal')
plt.plot(x_data, y_data, 'o', ms = 3, color = 'black', label = 'Actual')
plt.plot(x_data, w * x_data + b, '+', lw = 0.5, ms = 3, color = 'red', label = 'Prediction')
plt.legend()

# Fit data to figure and display
plt.tight_layout()
plt.show()