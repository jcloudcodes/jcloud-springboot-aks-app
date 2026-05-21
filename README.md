# jcloud-springboot-aks-app

Spring Boot application deployed to AKS with Jenkins shared library, Vault, Argo CD, Helm, External Secrets, and NGINX Ingress.

## Overview

This repository is the application repo.

Related repos:

- App repo:
  - [jcloud-springboot-aks-app](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/jenkins/jcloud-springboot-aks-app)
- Jenkins shared library:
  - [JavaShared_library](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/jenkins/JavaShared_library)
- GitOps repo:
  - [jcloud_argocd](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/jenkins/jcloud_argocd)

Main components used:

- Spring Boot
- Maven
- Docker
- AKS
- Helm
- Argo CD
- Vault
- External Secrets Operator
- Jenkins shared library
- ingress-nginx

## Working Deployment Flow

1. Build and test the Spring Boot app in Jenkins.
2. Build and push the Docker image.
3. Update the image tag in the GitOps repo.
4. Refresh Vault-backed ESO token flow if needed.
5. Bootstrap the Argo CD `Application`.
6. Sync Argo CD to deploy the Helm release into AKS.
7. External Secrets reads Vault and creates Kubernetes secrets.
8. The application starts in the `mss-dev` namespace.

## Jenkins App Configuration

This app consumes the Jenkins shared library through:

- [Jenkinsfile](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/jenkins/jcloud-springboot-aks-app/Jenkinsfile)

The app Jenkinsfile uses:

- `@Library('JavaShared_library@main') _`
- agent label:
  - `jslave-inbound`
- Maven tool:
  - `sharedMaven`

Important pipeline config used here:

- `appName: 'jcloud-springboot-aks-app'`
- `imageRepository: 'jcloudcodes/jcloud-springboot-aks-app'`
- `gitopsRepoUrl: 'https://gitlab.com/UDdeployTemplate/jcloud_argocd.git'`
- `gitopsBranch: 'main'`
- `helmValuesFile: 'environments/dev/values.yaml'`
- `argocdAppManifestFile: 'applications/mss-dev.yaml'`
- `argocdAppName: 'jcloud-springboot-aks-app'`
- `argocdServer: 'argocd.jcloudcodes.com'`
- `kubeNamespace: 'mss-dev'`
- `aksClusterName: 'sap-dev-aksdemo1'`

## AKS Terraform Notes

The AKS Terraform setup used for this environment is in:

- [applications/AI/demo/aks](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/applications/AI/demo/aks)

Important lesson:

- that Terraform folder expects the resource group and VNet/subnet to already exist
- it does not create the resource group or the network by itself

When the resource group was deleted, AKS creation failed with:

```text
ResourceGroupNotFound: Resource group 'rg-ai-demo-aks-dev' could not be found
```

After the resource group was recreated, AKS creation then failed with:

```text
Failed to get a VNet: vnet-ai-demo-aks-dev
```

That means these resources must exist before running that Terraform:

- resource group:
  - `rg-ai-demo-aks-dev`
- VNet:
  - `vnet-ai-demo-aks-dev`
- subnet:
  - `snet-aks`

Useful Azure checks:

```bash
az account list -o table
az account set --subscription "<SUBSCRIPTION_ID>"
az group list -o table
az network vnet list --resource-group rg-ai-demo-aks-dev -o table
az network vnet subnet list --resource-group rg-ai-demo-aks-dev --vnet-name vnet-ai-demo-aks-dev -o table
az aks list -o table
```

If the resource group is missing:

```bash
az group create --name rg-ai-demo-aks-dev --location eastus
```

If the VNet and subnet are missing:

```bash
az network vnet create \
  --resource-group rg-ai-demo-aks-dev \
  --name vnet-ai-demo-aks-dev \
  --address-prefix 10.0.0.0/16 \
  --subnet-name snet-aks \
  --subnet-prefix 10.0.1.0/24
```

Then rerun Terraform from the AKS folder:

```bash
terraform init
terraform plan
terraform apply
```

To get AKS credentials after cluster creation:

```bash
az aks get-credentials \
  --resource-group rg-ai-demo-aks-dev \
  --name sap-dev-aksdemo1 \
  --overwrite-existing

kubectl get nodes
```

## Vault Configuration

Vault is used for:

- Docker Hub credentials
- Argo CD credentials
- AKS kubeconfig
- application secrets
- MongoDB credentials

Vault secrets are stored in:

- mount:
  - `kv/jcloudcodes/java-web-app`
- path:
  - `jcloudcodes/java-web-app`

Important fields already used by the pipeline:

- `AKS_KUBECONFIG_B64`
- `ARGOCD_USERNAME`
- `ARGOCD_PASSWORD`
- `DOCKER_USERNAME`
- `DOCKER_PASSWORD`
- `GITLAB_TOKEN`
- `MONGO_DB_HOSTNAME`
- `MONGO_DB_USERNAME`
- `MONGO_DB_PASSWORD`
- `MONGO_INITDB_ROOT_USERNAME`
- `MONGO_INITDB_ROOT_PASSWORD`

### Store AKS kubeconfig in Vault

```bash
kubectl config view --minify --flatten > aks-kubeconfig.yaml
base64 < aks-kubeconfig.yaml | tr -d '\n' > aks-kubeconfig.b64

vault kv put -mount="kv/jcloudcodes/java-web-app" "jcloudcodes/java-web-app" \
  AKS_KUBECONFIG_B64="$(cat aks-kubeconfig.b64)"
```

### Jenkins Vault AppRole

Jenkins does not use the old GitLab JWT role.

Jenkins uses an AppRole.

Create the policy file:

```bash
cat > jenkins-java-web-app.hcl <<'EOF'
path "kv/jcloudcodes/java-web-app/data/jcloudcodes/java-web-app" {
  capabilities = ["read"]
}

path "auth/token/lookup-self" {
  capabilities = ["read"]
}

path "auth/token/create" {
  capabilities = ["create", "update"]
}

path "auth/token/create/*" {
  capabilities = ["create", "update"]
}
EOF
```

Write the policy:

```bash
export VAULT_ADDR="https://jcloudcodes-public-vault-e0a9d77c.e1f8f4d8.z1.hashicorp.cloud:8200"
export VAULT_NAMESPACE="admin"
export VAULT_TOKEN="<VAULT_ADMIN_TOKEN>"

vault policy write jenkins-java-web-app jenkins-java-web-app.hcl
vault policy read jenkins-java-web-app
```

Enable AppRole if needed:

```bash
vault auth list
vault auth enable approle
```

Create or update the Jenkins AppRole:

```bash
vault write auth/approle/role/jenkins-java-web-app \
  token_policies="jenkins-java-web-app,eso-mongo-read" \
  token_ttl="1h" \
  token_max_ttl="4h"
```

Get the AppRole values:

```bash
vault read -field=role_id auth/approle/role/jenkins-java-web-app/role-id
vault write -f -field=secret_id auth/approle/role/jenkins-java-web-app/secret-id
```

Store them in Jenkins as `Secret text` credentials:

- `vault-approle-role-id`
- `vault-approle-secret-id`

### Why the Jenkins Vault policy needed token create permissions

Without `auth/token/create`, Jenkins failed with:

```text
Code: 403
permission denied
```

After that permission was added, Vault then failed with:

```text
child policies must be subset of parent
```

That was fixed by attaching `eso-mongo-read` to the Jenkins AppRole itself:

```bash
vault write auth/approle/role/jenkins-java-web-app \
  token_policies="jenkins-java-web-app,eso-mongo-read" \
  token_ttl="1h" \
  token_max_ttl="4h"
```

## Jenkins Slave Tooling Notes

The Jenkins slave must be able to run:

- `git`
- `vault`
- `docker`

Maven is provided through Jenkins tool configuration:

- `sharedMaven`

The shared library was updated to run several deployment tools from Docker images instead of requiring host installs:

- `dtzar/helm-kubectl:3.19.1`
- `quay.io/argoproj/argocd:v3.4.1`
- `mikefarah/yq:4.53.2`

The Jenkins slave still needed `vault` on the host. After installing it under `/usr/local/bin`, the Jenkins user could not find it, so a symlink or PATH update was needed.

Example fix:

```bash
sudo ln -s /usr/local/bin/vault /usr/bin/vault
sudo -u jenkins which vault
sudo -u jenkins vault version
```

## Argo CD Installation

### Fresh install

```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

### Run Argo CD server behind ingress

Patch Argo CD server to run insecure internally:

```bash
kubectl -n argocd patch deployment argocd-server \
  --type='json' \
  -p='[
    {"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"--insecure"}
  ]'
```

Verify the args:

```bash
kubectl get deploy argocd-server -n argocd -o jsonpath='{.spec.template.spec.containers[0].args}'; echo
```

Expected output should include:

- `--insecure`

### Get Argo CD admin password

```bash
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d; echo
```

Username:

```bash
echo admin
```

### Argo CD CLI login

```bash
argocd login argocd.jcloudcodes.com \
  --username admin \
  --password '<PASSWORD>' \
  --insecure \
  --grpc-web
```

## Argo CD ApplicationSet CRD Fix

Argo CD core worked, but `argocd-applicationset-controller` crashed because the `ApplicationSet` CRD was missing.

Controller log showed:

```text
no matches for kind "ApplicationSet" in version "argoproj.io/v1alpha1"
```

Regular `kubectl apply` on the CRD failed with:

```text
metadata.annotations: Too long: may not be more than 262144 bytes
```

That happened because the CRD is large and client-side apply tried to store a huge `last-applied-configuration` annotation.

Use server-side apply instead:

```bash
kubectl apply --server-side -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/crds/applicationset-crd.yaml
```

Then restart the controller:

```bash
kubectl rollout restart deployment argocd-applicationset-controller -n argocd
kubectl get crd applicationsets.argoproj.io
kubectl get pods -n argocd
```

Useful Argo CD checks:

```bash
kubectl -n argocd get po
kubectl -n argocd get svc
kubectl -n argocd get ingress
kubectl -n argocd logs deploy/argocd-server
kubectl -n argocd logs pod/<applicationset-pod-name> --previous
```

## ingress-nginx Installation

Create values file for the Azure load balancer health probe:

```yaml
controller:
  service:
    annotations:
      service.beta.kubernetes.io/azure-load-balancer-health-probe-request-path: /healthz
```

Install ingress-nginx:

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

kubectl create namespace ingress-nginx --dry-run=client -o yaml | kubectl apply -f -

helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
  -n ingress-nginx \
  -f ingress-nginx-values.yaml
```

Check the external IP:

```bash
kubectl get svc -n ingress-nginx ingress-nginx-controller
```

The working environment exposed:

- `48.206.107.91`

If the annotation is missing and the service already exists, this also works:

```bash
kubectl annotate svc ingress-nginx-controller \
  -n ingress-nginx \
  service.beta.kubernetes.io/azure-load-balancer-health-probe-request-path=/healthz \
  --overwrite
```

## Argo CD Ingress

The stable working pattern for Argo CD behind nginx was:

- Argo CD server with `--insecure`
- ingress points to Argo CD service port `80`
- no `backend-protocol: HTTPS` annotation

Use:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: argocd-server-ingress
  namespace: argocd
spec:
  ingressClassName: nginx
  rules:
    - host: argocd.jcloudcodes.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: argocd-server
                port:
                  number: 80
```

Apply it:

```bash
kubectl apply -f argocd-ingress.yaml
kubectl get ingress -n argocd
kubectl describe ingress -n argocd argocd-server-ingress
```

DNS should point:

- `argocd.jcloudcodes.com` -> ingress external IP

Quick ingress test:

```bash
curl -I -H "Host: argocd.jcloudcodes.com" http://48.206.107.91
```

## Bootstrap the Argo CD Application

After Argo CD and the `Application` CRD are ready:

```bash
kubectl apply --validate=false -f applications/mss-dev.yaml
kubectl get applications -n argocd
```

If this fails with:

```text
no matches for kind "Application" in version "argoproj.io/v1alpha1"
```

it means Argo CD is not fully installed yet and the `applications.argoproj.io` CRD is missing.

Verify:

```bash
kubectl get crd applications.argoproj.io
```

## Useful Runtime Commands

### Kubernetes connectivity

```bash
kubectl get nodes
kubectl get ns
kubectl get pods -A
kubectl config current-context
```

### Service and ingress checks

```bash
kubectl get svc -A
kubectl get ingress -A
kubectl get endpoints -A
```

### App deployment checks

```bash
kubectl -n mss-dev get deploy
kubectl -n mss-dev get po
kubectl -n mss-dev get svc
kubectl -n mss-dev get secret
kubectl -n mss-dev describe deploy jcloud-springboot-aks-app
```

### External Secrets checks

```bash
kubectl get clustersecretstore
kubectl describe clustersecretstore vault-backend
kubectl -n mss-dev get externalsecret
kubectl -n mss-dev describe externalsecret
```

### Mongo checks

```bash
kubectl -n mss-dev get statefulset
kubectl -n mss-dev get pvc
kubectl -n mss-dev describe pvc
kubectl -n mss-dev describe pod mongo-0
```

## Issues Hit And Fixes

### 1. Jenkins shared library failed to load

Error:

```text
No version specified for library JavaShared_library
```

Fix:

```groovy
@Library('JavaShared_library@main') _
```

### 2. Declarative pipeline failed from `src/`

Error:

```text
Scripts not permitted to use method ... PlatformJavaAksPipeline agent ...
```

Fix:

- keep Declarative `pipeline {}` in:
  - [vars/platformJavaAksPipeline.groovy](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/jenkins/JavaShared_library/vars/platformJavaAksPipeline.groovy)
- keep helper logic in:
  - [src/org/jcloudcodes/pipelines/PlatformJavaAksPipeline.groovy](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/jenkins/JavaShared_library/src/org/jcloudcodes/pipelines/PlatformJavaAksPipeline.groovy)

### 3. Jenkins slave ran builds but missing tools

Errors seen:

```text
git: command not found
vault: command not found
mvn: command not found
```

Fix:

- install `git` on the slave
- expose `vault` to the Jenkins user
- use Jenkins Maven tool `sharedMaven`

### 4. Docker stage failed from Groovy property interpolation

Error:

```text
Scripts not permitted to use method groovy.lang.GroovyObject getProperty ... DOCKER_PASSWORD
```

Fix:

- read Docker credentials from Vault
- escape shell variables correctly in the shared library

### 5. GitOps repo auth failed even with correct token

Error:

```text
HTTP Basic: Access denied
```

Fix:

- the clone URL was using single quotes, so `$GITOPS_TOKEN` was not expanding
- changed the clone command to use double quotes in the shared library

### 6. AKS API resolved on host but not inside tool containers

Error:

```text
lookup sap-dev-aksdemo1-a02mre80.hcp.eastus.azmk8s.io: i/o timeout
```

Host test showed the AKS API was reachable:

```bash
curl -vk https://sap-dev-aksdemo1-a02mre80.hcp.eastus.azmk8s.io:443
ping -c 1 sap-dev-aksdemo1-a02mre80.hcp.eastus.azmk8s.io
```

Fix:

- run the tool containers with host networking:
  - `docker run --network host ...`

### 7. Argo CD login loop and 502/404 behavior

Working final direction:

- `argocd-server` runs with `--insecure`
- ingress points to service port `80`
- ingress-nginx service has Azure health probe annotation

### 8. ApplicationSet controller crash

Error:

```text
failed to get restmapping: no matches for kind "ApplicationSet" in version "argoproj.io/v1alpha1"
```

Fix:

```bash
kubectl apply --server-side -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/crds/applicationset-crd.yaml
kubectl rollout restart deployment argocd-applicationset-controller -n argocd
```

## Notes

- The shared library was intentionally tested in small steps first:
  - `Validate`
  - `Build`
  - `Test`
  - `Package`
- Then later stages were enabled one by one.
- The shared pipeline is currently configured to keep only the latest 5 Jenkins builds.
