@Library('JavaShared_library@main') _

 agent { label 'jslave-inbound' }
 
platformJavaAksPipeline(
  appName: 'jcloud-springboot-aks-app',
  agentLabel: 'jslave-inbound',
  mavenToolName: 'sharedMaven',
  javaVersion: '17',
  mavenCommand: 'mvn',
  mavenCliOpts: '-B -DskipTests=false',
  imageRepository: 'jcloudcodes/jcloud-springboot-aks-app',
  imageTag: env.BUILD_NUMBER,

  vaultAddr: 'https://jcloudcodes-public-vault-e0a9d77c.e1f8f4d8.z1.hashicorp.cloud:8200',
  vaultNamespace: 'admin',
  vaultKvMount: 'kv/jcloudcodes/java-web-app',
  vaultSecretPath: 'jcloudcodes/java-web-app',

  gitopsRepoUrl: 'https://gitlab.com/UDdeployTemplate/jcloud_argocd.git',
  gitopsBranch: 'main',
  helmValuesFile: 'environments/dev/values.yaml',

  argocdAppManifestFile: 'applications/mss-dev.yaml',
  argocdAppName: 'jcloud-springboot-aks-app',
  argocdServer: 'argocd.jcloudcodes.com',

  aksClusterName: 'sap-dev-aksdemo1',
  kubeNamespace: 'mss-dev',
  externalSecretsNamespace: 'external-secrets',

  helmChartPath: 'helm/jcloud-springboot-app',
  bootstrapArgoCdApp: true,
  refreshVaultToken: true,
  verifyEnvironment: true,

  sonarEnabled: false,
  nexusEnabled: false,
  helmPublishEnabled: false,
  gitlabRegistryEnabled: false,
  deployLinuxTomcat: false,
  deployWindowsTomcat: false,

  vaultRoleIdCredentialId: 'vault-approle-role-id',
  vaultSecretIdCredentialId: 'vault-approle-secret-id',
  dockerCredentialId: 'dockerhub-creds',
  gitopsRepoTokenCredentialId: 'gitops-repo-token'
)
