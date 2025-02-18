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

import com.google.android.fhir.db.impl.dao.LocalChangeToken
import com.google.android.fhir.db.impl.dao.SquashedLocalChange
import com.google.android.fhir.db.impl.entities.LocalChangeEntity
import org.hl7.fhir.r4.model.Bundle

typealias ResourceBundleAndAssociatedLocalChangeTokens = Pair<Bundle, List<LocalChangeToken>>

/**
 * Generates pairs of Transaction [Bundle] and [LocalChangeToken]s associated with the resources
 * present in the transaction bundle.
 */
internal open class TransactionBundleGenerator(
  val getBundleEntryComponentGeneratorForLocalChangeType:
    (type: LocalChangeEntity.Type) -> HttpVerbBasedBundleEntryComponentGenerator
) {

  fun generate(
    localChanges: List<List<SquashedLocalChange>>
  ): List<ResourceBundleAndAssociatedLocalChangeTokens> {
    return localChanges.filter { it.isNotEmpty() }.map { generateBundle(it) }
  }

  private fun generateBundle(
    localChanges: List<SquashedLocalChange>
  ): ResourceBundleAndAssociatedLocalChangeTokens {
    return Bundle().apply {
      type = Bundle.BundleType.TRANSACTION
      localChanges.forEach {
        this.addEntry(
          getBundleEntryComponentGeneratorForLocalChangeType(it.localChange.type).getEntry(it)
        )
      }
    } to localChanges.map { it.token }
  }

  companion object Factory {

    fun getDefault() = PutForCreateAndPatchForUpdateBasedTransactionGenerator

    /**
     * Returns a [TransactionBundleGenerator] based on the provided [Bundle.HTTPVerb]s for creating
     * and updating resources. The function may throw an [IllegalArgumentException] if the provided
     * [Bundle.HTTPVerb]s are not supported.
     */
    fun getGenerator(
      httpVerbToUseForCreate: Bundle.HTTPVerb,
      httpVerbToUseForUpdate: Bundle.HTTPVerb
    ): TransactionBundleGenerator {

      return if (httpVerbToUseForCreate == Bundle.HTTPVerb.PUT &&
          httpVerbToUseForUpdate == Bundle.HTTPVerb.PATCH
      ) {
        PutForCreateAndPatchForUpdateBasedTransactionGenerator
      } else {
        throw IllegalArgumentException(
          "Engine currently supports creation using [PUT] and updates using [PATCH]"
        )
      }
    }
  }
}

internal object PutForCreateAndPatchForUpdateBasedTransactionGenerator :
  TransactionBundleGenerator({ type ->
    when (type) {
      LocalChangeEntity.Type.INSERT -> HttpPutForCreateEntryComponentGenerator
      LocalChangeEntity.Type.UPDATE -> HttpPatchForUpdateEntryComponentGenerator
      LocalChangeEntity.Type.DELETE -> HttpDeleteEntryComponentGenerator
    }
  })
