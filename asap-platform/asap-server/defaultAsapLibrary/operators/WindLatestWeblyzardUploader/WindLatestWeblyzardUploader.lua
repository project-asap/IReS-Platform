operator = yarn {
  name = "WindLatestWeblyzardUploader",
  timeout = 10000,
  memory = 1024,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["description"] = {
       file = "asapLibrary/operators/WindLatestWeblyzardUploader/description",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["weblyzard_uploader.sh"] = {
       file = "asapLibrary/operators/WindLatestWeblyzardUploader/weblyzard_uploader.sh",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["weblyzard_uploader.py"] = {
       file = "asapLibrary/operators/WindLatestWeblyzardUploader/weblyzard_uploader.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
        base = "./weblyzard_uploader.sh"
    }
  }
}