# Deploy to AWS using Kubernetes
## Initialize AWS cloud with kops
- Install aws command line client
- Use following env variables
    ```
    export NAME=cbioportal.review.k8s.local
    export KOPS_STATE_STORE=s3://cbioportal-state-of-k8s # need to create this bucket in s3 first
    ```
- Make an S3 bucket for keeping track of K8s state
    ```
    aws s3api create-bucket --bucket ${KOPS_STATE_STORE/s3:\/\//}
    ```
- Install [kops](https://github.com/kubernetes/kops) (creates kubernetes
  cluster on amazon)
    ```
    kops create cluster --state=${KOPS_STATE_STORE} --cloud=aws \
        --zones=us-east-1a,us-east-1c --node-count=1  --node-size=t2.medium \
        --master-size=t2.small ${NAME}
    ```
