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

package com.google.android.fhir.sync.bundle

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.db.impl.dao.LocalChangeToken
import com.google.android.fhir.db.impl.dao.SquashedLocalChange
import com.google.android.fhir.db.impl.entities.LocalChangeEntity
import com.google.android.fhir.resource.TestingUtils
import com.google.android.fhir.sync.UploadResult
import com.google.common.truth.Truth
import java.net.ConnectException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.OperationOutcome
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.ResourceType
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BundleUploaderTest {

  @Test
  fun `upload Bundle transaction should emit Success`() = runBlocking {
    val result =
      BundleUploader(
          TestingUtils.BundleDataSource {
            Bundle().apply { type = Bundle.BundleType.TRANSACTIONRESPONSE }
          },
          TransactionBundleGenerator.getDefault()
        )
        .upload(localChanges)
        .toList()

    Truth.assertThat(result).hasSize(1)
    Truth.assertThat(result.first()).isInstanceOf(UploadResult.Success::class.java)
  }

  @Test
  fun `upload Bundle Transaction server error should emit Failure`() = runBlocking {
    val result =
      BundleUploader(
          TestingUtils.BundleDataSource {
            OperationOutcome().apply {
              addIssue(
                OperationOutcome.OperationOutcomeIssueComponent().apply {
                  code = OperationOutcome.IssueType.CONFLICT
                  diagnostics = "The resource has already been updated."
                }
              )
            }
          },
          TransactionBundleGenerator.getDefault()
        )
        .upload(localChanges)
        .toList()

    Truth.assertThat(result).hasSize(1)
    Truth.assertThat(result.first()).isInstanceOf(UploadResult.Failure::class.java)
  }

  @Test
  fun `upload Bundle transaction error during upload should emit Failure`() = runBlocking {
    val result =
      BundleUploader(
          TestingUtils.BundleDataSource { throw ConnectException("Failed to connect to server.") },
          TransactionBundleGenerator.getDefault()
        )
        .upload(localChanges)
        .toList()

    Truth.assertThat(result).hasSize(1)
    Truth.assertThat(result.first()).isInstanceOf(UploadResult.Failure::class.java)
  }

  companion object {
    val localChanges =
      listOf(
        SquashedLocalChange(
          LocalChangeToken(listOf(1)),
          LocalChangeEntity(
            id = 1,
            resourceType = ResourceType.Patient.name,
            resourceId = "Patient-001",
            type = LocalChangeEntity.Type.INSERT,
            payload =
              FhirContext.forCached(FhirVersionEnum.R4)
                .newJsonParser()
                .encodeResourceToString(
                  Patient().apply {
                    id = "Patient-001"
                    addName(
                      HumanName().apply {
                        addGiven("John")
                        family = "Doe"
                      }
                    )
                  }
                )
          )
        )
      )
  }
}
