package com.kironillo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.kironillo.domain.model.PermissionStatus
import com.kironillo.domain.model.PermissionType
import com.kironillo.domain.repository.HardwarePermissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel
    @Inject
    constructor(
        private val repository: HardwarePermissionRepository,
    ) : ViewModel() {
        fun getStatus(type: PermissionType): PermissionStatus = repository.getPermissionStatus(type)

        fun getRationale(type: PermissionType): String = repository.getRationale(type)

        fun isPermanentlyDenied(type: PermissionType): Boolean = repository.isPermanentlyDenied(type)
    }
