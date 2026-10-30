// Recursos de la aplicación dentro del resource group: ACR, Log Analytics (requerido por
// Container Apps), el entorno de Container Apps y la Container App del backend.
param location string
param acrName string
param containerAppsEnvName string
param containerAppName string
param containerImage string
param azureDocumentIntelligenceEndpoint string

@secure()
param azureDocumentIntelligenceKey string

param azureDocumentIntelligenceClassifierModelId string
param azureDocumentIntelligenceCedulaModelId string
param azureDocumentIntelligenceRutModelId string
param azureDocumentIntelligenceMinConfidence string

@secure()
param sendGridApiKey string

param sendGridFromEmail string
param sendGridFromName string

@description('Región para PostgreSQL y Storage. Puede ser distinta de "location": no comparten el bug de Container Apps Express, así que si una región se queda sin capacidad para un recurso, la otra no se ve afectada.')
param dataLocation string

param postgresServerName string
param postgresDatabaseName string
param postgresAdminUsername string

@secure()
param postgresAdminPassword string

param storageAccountName string
param blobContainerName string

resource acr 'Microsoft.ContainerRegistry/registries@2023-07-01' = {
  name: acrName
  location: location
  sku: {
    name: 'Basic'
  }
  properties: {
    // Los entornos "Express" de Container Apps (los que trae por defecto una suscripción de
    // estudiante) no soportan autenticación al registro por identidad administrada, así que
    // aquí se usa el usuario/clave de administrador del propio ACR en vez de RBAC.
    adminUserEnabled: true
  }
}

resource postgresServer 'Microsoft.DBforPostgreSQL/flexibleServers@2023-06-01-preview' = {
  name: postgresServerName
  location: dataLocation
  sku: {
    // El SKU más barato (Burstable) — tiene costo recurrente, a diferencia de todo lo demás en
    // este proyecto (Container Apps consumo, ACR, Document Intelligence F0, SendGrid gratis).
    name: 'Standard_B1ms'
    tier: 'Burstable'
  }
  properties: {
    administratorLogin: postgresAdminUsername
    administratorLoginPassword: postgresAdminPassword
    version: '16'
    storage: {
      storageSizeGB: 32
    }
    backup: {
      backupRetentionDays: 7
      geoRedundantBackup: 'Disabled'
    }
    highAvailability: {
      mode: 'Disabled'
    }
  }
}

resource postgresDatabase 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2023-06-01-preview' = {
  parent: postgresServer
  name: postgresDatabaseName
}

resource postgresFirewallAllowAzure 'Microsoft.DBforPostgreSQL/flexibleServers/firewallRules@2023-06-01-preview' = {
  parent: postgresServer
  name: 'AllowAllAzureServices'
  properties: {
    startIpAddress: '0.0.0.0'
    endIpAddress: '0.0.0.0'
  }
}

resource storageAccount 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: storageAccountName
  location: dataLocation
  kind: 'StorageV2'
  sku: {
    name: 'Standard_LRS'
  }
  properties: {
    minimumTlsVersion: 'TLS1_2'
    allowBlobPublicAccess: false
  }
}

resource blobService 'Microsoft.Storage/storageAccounts/blobServices@2023-01-01' = {
  parent: storageAccount
  name: 'default'
}

resource blobContainer 'Microsoft.Storage/storageAccounts/blobServices/containers@2023-01-01' = {
  parent: blobService
  name: blobContainerName
}

resource logAnalytics 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: '${containerAppsEnvName}-logs'
  location: location
  properties: {
    sku: {
      name: 'PerGB2018'
    }
    retentionInDays: 30
  }
}

resource containerAppsEnv 'Microsoft.App/managedEnvironments@2024-03-01' = {
  name: containerAppsEnvName
  location: location
  properties: {
    appLogsConfiguration: {
      destination: 'log-analytics'
      logAnalyticsConfiguration: {
        customerId: logAnalytics.properties.customerId
        sharedKey: logAnalytics.listKeys().primarySharedKey
      }
    }
  }
}

resource containerApp 'Microsoft.App/containerApps@2024-03-01' = {
  name: containerAppName
  location: location
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    managedEnvironmentId: containerAppsEnv.id
    configuration: {
      ingress: {
        external: true
        targetPort: 8080
        transport: 'auto'
      }
      registries: [
        {
          server: acr.properties.loginServer
          username: acr.listCredentials().username
          passwordSecretRef: 'acr-password'
        }
      ]
      secrets: [
        {
          name: 'doc-intel-key'
          value: azureDocumentIntelligenceKey
        }
        {
          name: 'acr-password'
          value: acr.listCredentials().passwords[0].value
        }
        {
          name: 'sendgrid-api-key'
          value: sendGridApiKey
        }
        {
          name: 'postgres-password'
          value: postgresAdminPassword
        }
        {
          name: 'storage-connection-string'
          value: 'DefaultEndpointsProtocol=https;AccountName=${storageAccount.name};AccountKey=${storageAccount.listKeys().keys[0].value};EndpointSuffix=core.windows.net'
        }
      ]
    }
    template: {
      containers: [
        {
          name: 'ms-tercero'
          image: containerImage
          resources: {
            cpu: json('0.5')
            memory: '1.0Gi'
          }
          env: [
            { name: 'AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT', value: azureDocumentIntelligenceEndpoint }
            { name: 'AZURE_DOCUMENT_INTELLIGENCE_API_KEY', secretRef: 'doc-intel-key' }
            { name: 'AZURE_DOCUMENT_INTELLIGENCE_CLASSIFIER_MODEL_ID', value: azureDocumentIntelligenceClassifierModelId }
            { name: 'AZURE_DOCUMENT_INTELLIGENCE_MODELS_CEDULA_MODEL_ID', value: azureDocumentIntelligenceCedulaModelId }
            { name: 'AZURE_DOCUMENT_INTELLIGENCE_MODELS_RUT_MODEL_ID', value: azureDocumentIntelligenceRutModelId }
            { name: 'AZURE_DOCUMENT_INTELLIGENCE_MIN_CONFIDENCE', value: azureDocumentIntelligenceMinConfidence }
            { name: 'SENDGRID_API_KEY', secretRef: 'sendgrid-api-key' }
            { name: 'SENDGRID_FROM_EMAIL', value: sendGridFromEmail }
            { name: 'SENDGRID_FROM_NAME', value: sendGridFromName }
            { name: 'POSTGRES_R2DBC_URL', value: 'r2dbc:postgresql://${postgresServer.properties.fullyQualifiedDomainName}:5432/${postgresDatabaseName}?sslMode=REQUIRE' }
            { name: 'POSTGRES_USERNAME', value: postgresAdminUsername }
            { name: 'POSTGRES_PASSWORD', secretRef: 'postgres-password' }
            { name: 'BLOB_STORAGE_CONNECTION_STRING', secretRef: 'storage-connection-string' }
            { name: 'BLOB_STORAGE_CONTAINER_NAME', value: blobContainerName }
          ]
          // El arranque real (Spring Data R2DBC + schema.sql contra Postgres real) tarda ~15s;
          // sin esto, Container Apps usaba su probe de startup por defecto (demasiado agresivo)
          // y mataba el contenedor antes de que el puerto llegara a abrirse.
          probes: [
            {
              type: 'Startup'
              httpGet: {
                path: '/actuator/health'
                port: 8080
              }
              initialDelaySeconds: 5
              periodSeconds: 5
              failureThreshold: 20
              timeoutSeconds: 5
            }
          ]
        }
      ]
      scale: {
        minReplicas: 0
        maxReplicas: 1
      }
    }
  }
}

output acrLoginServer string = acr.properties.loginServer
output containerAppFqdn string = containerApp.properties.configuration.ingress.fqdn
