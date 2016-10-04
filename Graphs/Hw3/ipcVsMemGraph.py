import numpy as np
import matplotlib.pyplot as plt
plt.style.use('ggplot')

N = 6
values = [1.0329974, 1.3893373, 1.7456772, 2.1020172, 2.458357, 2.8146968]
width = 0.8

ind = np.arange(N)
p1 = plt.bar(ind, values)

plt.ylabel('Instructions Per Cycle (IPC)')
plt.xlabel('Additional Memory Latency')
plt.title('IPC Versus Additional Memory Latency')
plt.xticks(ind + width / 2,
           ["0", "1", "2", "3", "4", "5"])
plt.show()
