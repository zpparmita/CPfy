package com.zeronfinity.core.usecase

import com.zeronfinity.core.repository.PlatformRepository

class GetPlatformImageUrl(private val platformRepository: PlatformRepository) {
    operator fun invoke(platformName: String) = platformRepository.getImageUrl(platformName)
}
