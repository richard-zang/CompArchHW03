import numpy as np
import matplotlib.pyplot as plt
plt.style.use('ggplot')

xVal = list(range(9,19))
# Width of line
width = 4.0
waysList = [[], [], [], [], []]

# Read our funny format. Sorry!
with open("onlyIPC.txt") as fin:
    for i in range(5):
        for j in range(10):
            waysList[i].append(float(fin.readline().split()[1]))

print(len(xVal))
p1, = plt.plot(xVal, waysList[0], linewidth=width, label="1-way")
p2, = plt.plot(xVal, waysList[1], linewidth=width,  label="2-way")
p3, = plt.plot(xVal, waysList[2], linewidth=width, label="4-way")
p4, = plt.plot(xVal, waysList[3], linewidth=width, label="8-way")
p5, = plt.plot(xVal, waysList[4], linewidth=width, label="16-way")
plt.legend(loc="lower right")

plt.ylabel('Instructions Per Cycle (IPC)')
plt.xlabel('log2(Cache size)')
plt.title('IPC vs. Cache Size')
plt.show()
