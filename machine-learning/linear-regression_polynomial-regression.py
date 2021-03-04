#!/usr/bin/env python

import numpy as np
import matplotlib.pyplot as plt
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import PolynomialFeatures
from sklearn.metrics import mean_squared_error, r2_score

m = 500
np.random.seed(seed = 5)
X = 6 * np.random.random(m).reshape(-1, 1) - 3
y = 0.5 * (X ** 5) - (X ** 3) - (X ** 2) + 2 + (5 * np.random.randn(m, 1))

# Split X data into train and test data
X_train = X[:300]
X_test = X[-200:]

# Split y data into train and test data
y_train = y[:300]
y_test = y[-200:]

# Linear Regression
model_linear = LinearRegression()
model_linear.fit(X_train, y_train)

y_train_linear_pred = model_linear.predict(X_train)
y_test_linear_pred = model_linear.predict(X_test)

# Polynomial Regression
plot_degrees = [2, 5, 8, 10, 20]
plot_data = []

loss_train_values = []
loss_test_values = []

degree_best = None;
loss_average_best = None;

for degree in range(2, 26):
  poly_features = PolynomialFeatures(degree = degree)
  X_train_poly = poly_features.fit_transform(X_train)
  X_test_poly = poly_features.fit_transform(X_test)

  model_poly = LinearRegression()
  model_poly.fit(X_train_poly, y_train)

  y_train_poly_pred = model_poly.predict(X_train_poly)
  y_test_poly_pred = model_poly.predict(X_test_poly)

  loss_train = mean_squared_error(y_train, y_train_poly_pred)
  loss_test = mean_squared_error(y_test, y_test_poly_pred)

  loss_train_values.append(loss_train)
  loss_test_values.append(loss_test)

  loss_average = (loss_train + loss_test) / 2

  if (loss_average_best is None) or (loss_average < loss_average_best):
    degree_best = degree
    loss_average_best = loss_average

  if degree in plot_degrees:
    plot_data.append({
      'degree': degree,
      'y_train_poly_pred': y_train_poly_pred,
      'y_test_poly_pred': y_test_poly_pred,
      'loss_train': loss_train,
      'loss_test': loss_test
    })

# Plot prediction figures for each degree in plot_degrees
for data in plot_data:
  plt.figure()
  plt.title(f"Degree = {data['degree']} | Train Loss = {data['loss_train']:#0.1f} | Test Loss = {data['loss_test']:#0.1f}", fontweight = 'bold')
  plt.xlim(-3.5, 3.5)
  plt.xlabel('x', fontstyle = 'italic')
  plt.ylim(-115, 100)
  plt.ylabel('y', fontstyle = 'italic', rotation = 'horizontal', labelpad = 10)
  plt.scatter(X_test, y_test, color = 'black')
  plt.plot(X_test, y_test_linear_pred, color = 'blue', linewidth = 3, label = 'Train')
  plt.plot(X_test, data['y_test_poly_pred'], color = 'red', linewidth = 3, label = 'Test')
  plt.legend()

# Plot degree vs loss figure
plt.figure()
plt.title('Degree vs Loss', fontweight = 'bold')
plt.xlim(0, 27)
plt.xlabel('Degree', fontstyle = 'italic')
plt.ylim(0, 350)
plt.ylabel('Loss', fontstyle = 'italic', rotation = 'horizontal', labelpad = 20)
plt.plot(np.arange(2, 26, 1), loss_train_values, color = 'green', linewidth = 3, label = 'Train')
plt.plot(np.arange(2, 26, 1), loss_test_values, color = 'blue', linewidth = 3, label = 'Test')
plt.plot([degree_best], [loss_average_best], 'o', ms = 4, color = 'red', label = "Best")
plt.annotate(f"Degree = {degree_best}, Avg Loss = {loss_average_best:#0.1f}", xy = (degree_best, loss_average_best), xytext = (degree_best + 0.2, loss_average_best + 10))
plt.legend()

# Fit data to figure and display
plt.tight_layout()
plt.show()