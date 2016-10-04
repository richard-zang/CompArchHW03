import numpy as np
import matplotlib.pyplot as plt
plt.style.use('ggplot')

bimodalFile = "bimodal.data"
gshareFile = "gshare.data"
tournFile = "tourn.data"
xVal = list(range(4,19))
width = 4.0

with open(bimodalFile) as fin:
    bimodalVal = [float(x.split()[1]) for x in fin]
with open(gshareFile) as fin:
    gshareVal = [float(x.split()[1]) for x in fin]
with open(tournFile) as fin:
    tournVal = [float(x.split()[1]) for x in fin]

p1, = plt.plot(xVal, bimodalVal, linewidth=width, label="Bimodal")
p2, = plt.plot(xVal, gshareVal, linewidth=width,  label="Gshare")
p3, = plt.plot(xVal, tournVal, linewidth=width, label="Tournament")
plt.legend(loc="lower right")

plt.ylabel('Instructions Per Cycle (IPC)')
plt.xlabel('log2(Predictor Size)')
plt.title('Predictor Size Vs. IPC')
plt.show()
