import socket, xmltodict
from tools import  mycast
from time import sleep, time


class GMonitor:

    def __init__(self, endpoint, summarized=False, cast=True, hosts=None):

        self.endpoint = endpoint

        # totals
        self.iops_rd_total = self.iops_wt_total = self.net_in_total = self.net_out_total =  0
        # the time-lines of metric values
        self.metrics_timeline, self.metrics_summaries_timeline = [], []
        self.summarized = summarized
        self.iterations = 0
        self.cast = cast
        self.start_time = time()
        self.hosts = hosts

    def get_all_metrics(self):
        """
        Wrapper for static method "get_all_metrics" that uses this GMonitors endpoint and keeps only the hosts
        in filter, if "hosts" is defined
        :return:
        """
        all_metrics =  get_all_metrics(self.endpoint,self.cast)

        # if "hosts" is not defined we are keeping everything
        if not self.hosts: return all_metrics

        # filter that keeps only the hosts ('h') included in "hosts"
        # also works if "hosts" is string instead of iterable
        host_filt = lambda (h,m): h in self.hosts
        # apply filter
        key_vals = filter(host_filt, all_metrics.iteritems())
        # return a dict of the results
        return dict(key_vals)

    def get_all_metrics_and_summary(self):
        """
        wrapper function that gets both all unprocessed metrics from ganglia and
        also their summary, and adds the summary as an extra field
        :param endpoint:
        :param cast:
        :return:
        """
        raw_metrics = self.get_all_metrics()
        summary = self.get_summary(raw_metrics=raw_metrics)

        return raw_metrics, summary

    def update_metrics(self):

            # int looses some precision but looks better :-|
            time_elapsed = int(time()-self.start_time)

            if self.summarized:
                metric_summaries = self.get_summary()
                raw_metrics = None
            else:
                raw_metrics, metric_summaries = self.get_all_metrics_and_summary()

            if raw_metrics:
                self.metrics_timeline.append((time_elapsed, raw_metrics))
            if metric_summaries:
                self.metrics_summaries_timeline.append((time_elapsed,metric_summaries))

            self.iterations += 1

    def get_summary(self, raw_metrics=None):
        """
        From the available ganglia metrics returns only the useful ones
        """

        allmetrics = self.get_all_metrics() if not raw_metrics else raw_metrics

        if not allmetrics : return None

        cpu = mem = net_in = net_out = iops_read = iops_write = 0

        for k, v in allmetrics.items():
            try:
                cpu += 100-float(v["cpu_idle"])
                total_mem = float(v["mem_free"]) + float(v["mem_buffers"]) +  float(v["mem_cached"])
                mem += 1.0 - float(v["mem_free"])/total_mem
                net_in += float(v["bytes_in"])
                net_out += float(v["bytes_out"])
                iops_read +=float( v.get("io_read", "-1"))
                iops_write +=float( v.get("io_write", "-1"))
            except: continue

        host_count = len(allmetrics.keys())

        self.iops_wt_total+= iops_write if iops_write > 0 else 0
        self.iops_rd_total+= iops_read if iops_read > 0 else 0
        self.net_in_total+= net_in
        self.net_out_total += net_out

        return {
            "cpu": cpu / host_count,
            "mem": 100 * mem / host_count,
            "net_in": net_in,
            "net_out": net_out,
            "kbps_read": iops_read / host_count,
            "kbps_write": iops_write / host_count
        }




### static helper methods

def get_all_metrics(endpoint, cast=True):
        attempts = 0
        while attempts <= 3:
            attempts += 1
            try:
                s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                s.connect(endpoint)

                # get raw xml from socket
                xml = ""
                while 1:
                    data = s.recv(1024)
                    if len(data)==0: break
                    xml+= data
                s.close()

                # parse xml to dict
                parsed = xmltodict.parse(xml)

                hosts = parsed["GANGLIA_XML"]["CLUSTER"]["HOST"]

                allmetrics = {}
                for h in hosts:
                    host = h["@NAME"]
                    if "METRIC" not in h: continue
                    # create a dict of metrics
                    t = map(lambda x: (x["@NAME"],x["@VAL"]) , h["METRIC"] )
                    metrics = dict( (k,v) for k,v in t )
                    allmetrics[host] = metrics
                if cast:
                    return mycast(allmetrics)
                else:
                    return allmetrics

            except Exception as e:
                template = "An exception of type {0} occured. Arguments:\n{1!r}"
                message = template.format(type(e).__name__, e.args)
                print message
                sleep(0.5)
        return None



if __name__ == "__main__":
    # simple debug/testing
    mon = GMonitor(("master",8649), hosts="master" )
    from pprint import pprint
    pprint( mon.get_all_metrics() )