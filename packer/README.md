# Creating brendaweb2 images with packer and ansible

## How to build
- AWS AMI: ```packer build -only=amazon-ebs ubuntu-16.04-amd64.json```

- Local VirtualBox image: ```packer build -only=virtualbox-iso ubuntu-16.04-amd64.json```


