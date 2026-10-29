// Despliega toda la infraestructura de ms-tercero en una cuenta de Azure nueva:
// resource group + ACR + entorno de Container Apps + la Container App del backend.
// Pensado para poder replicarse igual en otra cuenta corriendo un solo comando.
targetScope = 'subscription'

@description('Ubicación para todos los recursos. Algunas suscripciones (ej. Azure for Students) restringen las regiones permitidas por política: revisa "az policy assignment list" si el despliegue falla con RequestDisallowedByAzure. Se eligió belgiumcentral porque, de las regiones permitidas en esta suscripción, es la única que queda fuera del rollout de Azure Container Apps "Express" (ver infra/README o el tema de estudio sobre IaC/CI-CD): Express no soporta pull de imágenes privadas del ACR, ni identidad administrada del sistema, ni revisionSuffix.')
param location string = 'belgiumcentral'

@description('Nombre del resource group')
param resourceGroupName string = 'rg-ms-tercero'

@description('Nombre del Azure Container Registry. Debe ser único a nivel global (solo letras y números).')
param acrName string

@description('Nombre del entorno de Container Apps')
param containerAppsEnvName string = 'cae-ms-tercero'

@description('Nombre de la Container App del backend')
param containerAppName string = 'ms-tercero-api'

@description('Imagen inicial del contenedor. El pipeline de CI/CD la actualiza después del primer despliegue.')
param containerImage string = 'mcr.microsoft.com/k8se/quickstart:latest'

@description('Endpoint del recurso de Azure AI Document Intelligence')
param azureDocumentIntelligenceEndpoint string

@secure()
@description('API key del recurso de Azure AI Document Intelligence. Nunca se guarda en un archivo del repo: se pasa en el momento del despliegue.')
param azureDocumentIntelligenceKey string

@description('Nombre del modelo clasificador en Document Intelligence Studio')
param azureDocumentIntelligenceClassifierModelId string = 'model1'

@description('Nombre del modelo extractor de cédula en Document Intelligence Studio')
param azureDocumentIntelligenceCedulaModelId string = 'model_2'

@description('Nombre del modelo extractor de RUT en Document Intelligence Studio')
param azureDocumentIntelligenceRutModelId string = 'model_1'

@description('Texto exacto que el modelo de RUT devuelve en tipo_contribuyente para persona natural')
param rutValidationTipoContribuyenteNatural string = 'Persona natural o sucesión ilíquida'

@description('Texto exacto que el modelo de RUT devuelve en tipo_contribuyente para persona jurídica')
param rutValidationTipoContribuyenteJuridica string = 'Persona Jurídica'

@description('Confianza mínima (0-1) que Azure Document Intelligence debe reportar para aceptar una clasificación o un campo extraído. Bajado de 0.85 a 0.80 tras ver en producción un RUT real clasificado con 0.81 de confianza.')
param azureDocumentIntelligenceMinConfidence string = '0.80'

@secure()
@description('API key de SendGrid (Settings > API Keys). Nunca se guarda en un archivo del repo: se pasa en el momento del despliegue.')
param sendGridApiKey string

@description('Remitente verificado en SendGrid (Settings > Sender Authentication). El envío falla si no está verificado.')
param sendGridFromEmail string

@description('Nombre que aparece como remitente en el correo de confirmación.')
param sendGridFromName string = 'Universidad El Bosque'

resource rg 'Microsoft.Resources/resourceGroups@2024-03-01' = {
  name: resourceGroupName
  location: location
}

module appResources 'modules/app-resources.bicep' = {
  name: 'ms-tercero-app-resources'
  scope: rg
  params: {
    location: location
    acrName: acrName
    containerAppsEnvName: containerAppsEnvName
    containerAppName: containerAppName
    containerImage: containerImage
    azureDocumentIntelligenceEndpoint: azureDocumentIntelligenceEndpoint
    azureDocumentIntelligenceKey: azureDocumentIntelligenceKey
    azureDocumentIntelligenceClassifierModelId: azureDocumentIntelligenceClassifierModelId
    azureDocumentIntelligenceCedulaModelId: azureDocumentIntelligenceCedulaModelId
    azureDocumentIntelligenceRutModelId: azureDocumentIntelligenceRutModelId
    rutValidationTipoContribuyenteNatural: rutValidationTipoContribuyenteNatural
    rutValidationTipoContribuyenteJuridica: rutValidationTipoContribuyenteJuridica
    azureDocumentIntelligenceMinConfidence: azureDocumentIntelligenceMinConfidence
    sendGridApiKey: sendGridApiKey
    sendGridFromEmail: sendGridFromEmail
    sendGridFromName: sendGridFromName
  }
}

output acrLoginServer string = appResources.outputs.acrLoginServer
output containerAppFqdn string = appResources.outputs.containerAppFqdn
