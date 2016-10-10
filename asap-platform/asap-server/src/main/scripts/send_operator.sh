LOCAL_OPERATOR=$1
HOST=$2
OPERATOR_NAME=$3
TARBALL="${OPERATOR_NAME}.tar.gz"

tar -cf $TARBALL --directory="$LOCAL_OPERATOR" .
curl -H "Content-Type: application/octet-stream" -X POST --data-binary @$TARBALL $HOST:1323/operators/addTarball?opname=$OPERATOR_NAME

printf "\n"
