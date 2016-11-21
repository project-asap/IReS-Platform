__author__ = 'cmantas'
from tools import *
from json import loads

ms = take_single("select metrics from mahout_kmeans_text where k=15  and documents=90300 and dimensions=53235;")[0]
mj = loads(ms)

cols = iter(["#727272", '#f1595f', '#79c36a', '#599ad3', '#f9a65a','#9e66ab','#cd7058', '#d77fb3'])


def timeline2vaslues(fieldname, metrics):
    times =[]
    values =[]
    for k,v in metrics:
        times.append(k)
        values.append(v[fieldname])
    return times, values

def sum_timeline_vals(fieldnames, metrics):
    times =[]
    values =[]
    for k,v in metrics:
        times.append(k)
        sum = 0
        for i in fieldnames:
            if i.startswith("kbps"):
                v[i]=int(v[i])
            sum += v[i]
        values.append(sum)
    return times, values


# figure()
fig, ax1 = plt.subplots()

times, values = timeline2vaslues("cpu", mj)
d, = ax1.plot(times, values, color=next(cols))
ax1.set_ylabel('percentage (%)')
times, values = timeline2vaslues("mem", mj)
a, = ax1.plot(times, values, color=next(cols))

ax2 = ax1.twinx()
times, values = sum_timeline_vals(["kbps_read", "kbps_write"], mj)
ax2.set_ylabel("KB/s")
b, = ax2.plot(times, values, color=next(cols))

times, values = sum_timeline_vals(["net_in", "net_out"], mj)
c, = ax2.plot(times, values, color=next(cols))
plt.title("Mahout K-means Cluster Metrics")
plt.legend([d, a, b,c], ["CPU", "MEM", "Disk IO", "Net IO"], loc=3)




show()