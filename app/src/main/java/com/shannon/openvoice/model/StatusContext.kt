
package com.shannon.openvoice.model

data class StatusContext(
    val ancestors: List<StatusModel>,
    val descendants: List<StatusModel>
)
