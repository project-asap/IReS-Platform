operator = yarn {
  name = "WindLatestPeakDetectionSparkNested",
  timeout = 10000,
  memory = 4096,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["description"] = {
       file = "asapLibrary/operators/WindLatestPeakDetectionSparkNested/description",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["joda-time-2.8.2.jar"] = {
       file = "asapLibrary/operators/WindLatestPeakDetectionSparkNested/joda-time-2.8.2.jar",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["telecomanalytics_2.10-1.1.jar"] = {
       file = "asapLibrary/operators/WindLatestPeakDetectionSparkNested/telecomanalytics_2.10-1.1.jar",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["spark-assembly-1.6.0-SNAPSHOT-hadoop2.2.0.jar"] = {
       file = "asapLibrary/operators/WindLatestPeakDetectionSparkNested/spark-assembly-1.6.0-SNAPSHOT-hadoop2.2.0.jar",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["field2col_simulated.csv"] = {
       file = "asapLibrary/operators/WindLatestPeakDetectionSparkNested/field2col_simulated.csv",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["submit.sh"] = {
       file = "asapLibrary/operators/WindLatestPeakDetectionSparkNested/submit.sh",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
        base = "./submit.sh"
    }
  }
}