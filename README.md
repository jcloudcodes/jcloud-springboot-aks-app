# jcloud-springboot-aks-app

Spring Boot application with MongoDB deployed to AKS through GitLab CI, Vault, Argo CD, Helm, and External Secrets.

## Overview

This project uses:

- Spring Boot for the application
- MongoDB as a StatefulSet
- Helm for packaging Kubernetes resources
- Argo CD for GitOps delivery
- Vault for secret storage
- External Secrets Operator to sync Vault values into Kubernetes secrets
- GitLab CI templates for build, image publish, GitOps update, Argo CD bootstrap, deploy, and verification

Current related repositories:

- App repo:
  - [jcloud-springboot-aks-app](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/applications/AI/jcloud-springboot-aks-app)
- GitOps repo:
  - [jcloud_argocd](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/applications/AI/jcloud_argocd)
- Shared CI templates:
  - [jcloudcodes_template/gitlab-ci](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/applications/AI/jcloudcodes_template/gitlab-ci)

## Deployment Flow

The intended deployment flow is:

1. Commit code to the Spring Boot app repo.
2. GitLab CI builds and tests the app.
3. Docker image is built and pushed.
4. GitLab CI updates the image tag in the GitOps repo.
5. GitLab CI bootstraps the Argo CD `Application` if needed.
6. Argo CD syncs the Helm chart from the GitOps repo to AKS.
7. External Secrets reads Vault and creates Kubernetes secrets.
8. Spring Boot and MongoDB start in the target namespace.

## Important Files

App repo:

- [Dockerfile](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/applications/AI/jcloud-springboot-aks-app/Dockerfile)
- [.gitlab-ci.yml](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/applications/AI/jcloud-springboot-aks-app/.gitlab-ci.yml)
- [helm/jcloud-springboot-app](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/applications/AI/jcloud-springboot-aks-app/helm/jcloud-springboot-app)

GitOps repo:

- [applications/mss-dev.yaml](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/applications/AI/jcloud_argocd/applications/mss-dev.yaml)
- [environments/dev/values.yaml](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/applications/AI/jcloud_argocd/environments/dev/values.yaml)
- [platform/external-secrets/vault-clustersecretstore.yaml](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/applications/AI/jcloud_argocd/platform/external-secrets/vault-clustersecretstore.yaml)

Shared AKS CI template:

- [.gitops-aks.jobs.yml](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/applications/AI/jcloudcodes_template/gitlab-ci/templates/.gitops-aks.jobs.yml)

## GitLab CI Variables

Main variables used in the AKS app pipeline:

- `APP_NAME`
- `IMAGE_REPOSITORY`
- `IMAGE_TAG`
- `VAULT_ADDR`
- `VAULT_NAMESPACE`
- `VAULT_ROLE`
- `VAULT_KV_MOUNT`
- `VAULT_SECRET_PATH`
- `AKS_CLUSTER_NAME`
- `KUBE_NAMESPACE`
- `HELM_REPO_URL`
- `HELM_VALUES_FILE`
- `ARGOCD_APP_MANIFEST_FILE`
- `ARGOCD_SERVER`
- `ARGOCD_APP_NAME`
- `EXTERNAL_SECRETS_NAMESPACE` if different from `external-secrets`

Vault secret fields used:

- `GITLAB_TOKEN`
- `ARGOCD_USERNAME`
- `ARGOCD_PASSWORD`
- `AKS_KUBECONFIG_B64`
- `MONGO_DB_HOSTNAME`
- `MONGO_DB_USERNAME`
- `MONGO_DB_PASSWORD`
- `MONGO_INITDB_ROOT_USERNAME`
- `MONGO_INITDB_ROOT_PASSWORD`
- `DOCKER_USERNAME`
- `DOCKER_PASSWORD`

## Useful Commands

### AKS credentials from Mac

```bash
az account list -o table
az account set --subscription "<SUBSCRIPTION_ID>"
az aks list -o table
az aks get-credentials --resource-group "<RESOURCE_GROUP>" --name "<AKS_CLUSTER_NAME>" --overwrite-existing
kubectl get nodes
```

### Store AKS kubeconfig in Vault

```bash
kubectl config view --minify --flatten > aks-kubeconfig.yaml
base64 < aks-kubeconfig.yaml | tr -d '\n' > aks-kubeconfig.b64
vault kv put -mount="$VAULT_KV_MOUNT" "$VAULT_SECRET_PATH" AKS_KUBECONFIG_B64="$(cat aks-kubeconfig.b64)"
```

### Argo CD checks

```bash
argocd login argocd.jcloudcodes.com --username admin --password '<PASSWORD>' --insecure --grpc-web
argocd app list --grpc-web
argocd app get jcloud-springboot-aks-app --grpc-web
argocd app sync jcloud-springboot-aks-app --grpc-web
```

### Storage and Mongo checks

```bash
kubectl get storageclass
kubectl -n mss-dev get pvc
kubectl -n mss-dev describe pvc mongo-data-mongo-0
kubectl -n mss-dev describe pod mongo-0
```

### External Secrets checks

```bash
kubectl get deploy -A | grep external-secrets
kubectl get externalsecret -n mss-dev
kubectl get clustersecretstore
kubectl describe clustersecretstore vault-backend
kubectl get secret -n mss-dev
```

## Troubleshooting History

### 1. GitLab shared runner minutes exhausted

Error:

```text
The jcloudcodes namespace has reached its shared runner compute minutes quota
```

Fix:

- Installed a self-managed GitLab Runner on the RHEL VM.
- Registered it as a group-level runner.
- Installed missing tools for the shell runner:
  - `python3-pip`
  - `docker`
  - `kubectl`
  - `yq`

Why:

- Shell runners use the VM tools directly.
- Shared runner minutes do not apply to self-managed runners.

### 2. GitLab runner picked up jobs but VM tools were missing

Errors:

```text
python3.12: command not found
/usr/bin/python3: No module named pip
docker: command not found
kubectl: command not found
yq: command not found
```

Fix:

- Installed the required packages and Docker engine on the runner VM.

Why:

- `image:` and `services:` do not provide tools for a shell executor.

### 3. Vault JWT login failed because of wrong project binding

Error:

```text
claim "project_id" does not match any associated bound claim values
```

Fix:

- Updated the Vault JWT role to include the new GitLab `project_id`.

Example:

```json
"bound_claims": {
  "project_id": ["81669677", "81838176", "82248219"]
}
```

Why:

- Vault JWT auth was restricted to specific GitLab project IDs.

### 4. AKS and GKE template collision

Problem:

- The shared Java Maven pipeline was including both GKE and AKS GitOps templates.

Fix:

- Removed automatic inclusion of both cluster templates from:
  - [pipelines/java-maven/.java.maven.pipeline.yml](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/applications/AI/jcloudcodes_template/gitlab-ci/pipelines/java-maven/.java.maven.pipeline.yml)
- Left cluster selection to each app repo.

Why:

- Each app should choose its own cluster-specific deployment template.

### 5. Missing Argo CD application

Problem:

- The pipeline could log in to Argo CD, but the target app did not exist.

Fix:

- Added bootstrap flow in the AKS template to apply the app manifest from the GitOps repo:
  - `.bootstrap_argocd_app`
- Updated the app pipeline to use:
  - `bootstrap:argocd-app`

Why:

- Argo CD cannot sync an app until the `Application` resource itself exists.

### 6. Wrong app manifest in the GitOps repo

Problems found in [applications/mss-dev.yaml](/Users/makutaworldmpm/Desktop/eagunu_2025/jcloudcodes/applications/AI/jcloud_argocd/applications/mss-dev.yaml):

- wrong app name
- wrong chart path
- wrong namespace

Fix:

- Updated it to:
  - app name: `jcloud-springboot-aks-app`
  - chart path: `helm/mss-mongodb-app`
  - namespace: `mss-dev`

Why:

- The GitOps app must point to the actual chart and correct target namespace.

### 7. Argo CD app existed but GitOps repo auth failed

Error:

```text
authentication required: HTTP Basic: Access denied
```

Fix:

- Added the missing GitOps repo to Argo CD with valid GitLab token credentials:

```bash
argocd repo add https://gitlab.com/UDdeployTemplate/jcloud_argocd.git \
  --username YOUR_GITLAB_USERNAME \
  --password YOUR_GITLAB_TOKEN \
  --grpc-web
```

Why:

- Argo CD repo credentials are separate from GitLab CI repo access.

### 8. Vault token refresh job failed on token creation

Error:

```text
vault token create ... permission denied
```

Fix:

- Updated the AKS template to fall back to the JWT-authenticated token if dedicated ESO token creation is denied.

Why:

- This avoided a hard pipeline stop and allowed the cluster-side ESO issue to be debugged next.

### 9. `external-secrets` namespace did not exist

Error:

```text
namespaces "external-secrets" not found
```

Fix:

- Updated the AKS template to create the namespace if missing and to use `EXTERNAL_SECRETS_NAMESPACE` if set.

Why:

- The pipeline was assuming the namespace already existed.

### 10. External Secrets operator existed but Vault store was missing or invalid

Observed:

```bash
kubectl get externalsecret -n mss-dev
kubectl get clustersecretstore
kubectl describe clustersecretstore vault-backend
```

Errors seen:

- `SecretSyncedError`
- `InvalidProviderConfig`
- `invalid vault credentials`
- `auth/token/lookup-self`
- `403 permission denied`

Fixes:

- Ensured `ClusterSecretStore` `vault-backend` existed.
- Verified it used:
  - secret `vault-token`
  - namespace `external-secrets`
- Created a proper Vault policy and token for ESO.

Example policy:

```hcl
path "kv/jcloudcodes/java-web-app/data/jcloudcodes/java-web-app" {
  capabilities = ["read"]
}

path "auth/token/lookup-self" {
  capabilities = ["read"]
}
```

Why:

- ESO needs a valid Vault token and the right policy to fetch secrets and validate itself.

### 11. `mongo-secret` and `dockerhub-secret` were missing

Observed pod errors:

```text
Error: secret "mongo-secret" not found
Unable to retrieve some image pull secrets (dockerhub-secret)
```

What we confirmed:

- The chart in the GitOps repo really does create:
  - `ExternalSecret` for `mongo-secret`
  - `ExternalSecret` for `dockerhub-secret`

Why the secrets were still missing:

- Argo CD applies `ExternalSecret` resources.
- External Secrets Operator must then read Vault and create actual Kubernetes `Secret` objects.
- If the Vault store or token is broken, the `Secret` objects are never materialized.

### 12. MongoDB pod pending because of invalid storage class

Error:

```text
pod has unbound immediate PersistentVolumeClaims
```

Observed storage classes in AKS:

- `default`
- `managed`
- `managed-csi`
- `managed-csi-premium`
- others

Problem in GitOps values:

```yaml
mongo:
  persistence:
    storageClassName: standard-rwo
```

Fix:

```yaml
mongo:
  persistence:
    storageClassName: managed-csi
    size: 10Gi
```

Why:

- `standard-rwo` did not exist in AKS.

### 13. StatefulSet immutable field error after storage class change

Error:

```text
StatefulSet.apps "mongo" is invalid: spec: Forbidden
```

Fix:

```bash
kubectl -n mss-dev delete statefulset mongo --cascade=orphan
kubectl -n mss-dev delete pvc mongo-data-mongo-0
argocd app sync jcloud-springboot-aks-app --grpc-web
```

Why:

- Changing `volumeClaimTemplates` is not allowed in-place on a StatefulSet.

## Current AKS Notes

- AKS storage class should use `managed-csi` for MongoDB PVCs unless the cluster standard changes.
- `AKS_KUBECONFIG_B64` is stored in Vault and restored into `~/.kube/config` in CI.
- Argo CD sync depends on the GitOps repo being registered in Argo CD with valid GitLab credentials.
- External Secrets requires:
  - operator running
  - valid `ClusterSecretStore`
  - valid `vault-token`
  - Vault policy that can read the configured secret path

## Recommended Next Improvements

- Keep AKS and GKE templates separate by app selection, not shared auto-include.
- Manage External Secrets installation and `ClusterSecretStore` bootstrap through GitOps as platform components.
- Use dedicated Vault policies and a renewable token path for ESO.
- Document exact Vault secret keys and ownership in a platform runbook.
- Consider managed MongoDB for production instead of in-cluster MongoDB.

## Quick Recovery Commands

Resync the app:

```bash
argocd app sync jcloud-springboot-aks-app --grpc-web
argocd app wait jcloud-springboot-aks-app --health --sync --timeout 600 --grpc-web
```

Check app status:

```bash
argocd app get jcloud-springboot-aks-app --grpc-web
kubectl get pods -n mss-dev
kubectl get pvc -n mss-dev
kubectl get externalsecret -n mss-dev
kubectl get secret -n mss-dev
```

Check Mongo state:

```bash
kubectl -n mss-dev describe pod mongo-0
kubectl -n mss-dev describe pvc mongo-data-mongo-0
```
