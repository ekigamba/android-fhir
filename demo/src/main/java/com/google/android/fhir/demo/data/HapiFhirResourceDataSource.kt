/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.fhir.demo.data

import com.google.android.fhir.ContentTypes
import com.google.android.fhir.demo.api.HapiFhirService
import com.google.android.fhir.sync.DataSource
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.OperationOutcome
import org.hl7.fhir.r4.model.Resource

val MEDIA_TYPE_FHIR_JSON = ContentTypes.APPLICATION_FHIR_JSON.toMediaType()
val MEDIA_TYPE_JSON_PATCH = ContentTypes.APPLICATION_JSON_PATCH.toMediaType()

/** Implementation of the [DataSource] that communicates with hapi fhir. */
class HapiFhirResourceDataSource(private val service: HapiFhirService) : DataSource {

  override suspend fun loadData(path: String): Bundle {
    return service.getResource(path)
  }

  override suspend fun insert(resourceType: String, resourceId: String, payload: String): Resource {
    return service.insertResource(
      resourceType,
      resourceId,
      payload.toRequestBody(MEDIA_TYPE_FHIR_JSON)
    )
  }

  override suspend fun update(
    resourceType: String,
    resourceId: String,
    payload: String
  ): OperationOutcome {
    return service.updateResource(
      resourceType,
      resourceId,
      payload.toRequestBody(MEDIA_TYPE_JSON_PATCH)
    )
  }

  override suspend fun delete(resourceType: String, resourceId: String): OperationOutcome {
    return service.deleteResource(resourceType, resourceId)
  }

  override suspend fun postBundle(payload: String): Resource {
    return service.postData(payload.toRequestBody(MEDIA_TYPE_FHIR_JSON))
  }
}
