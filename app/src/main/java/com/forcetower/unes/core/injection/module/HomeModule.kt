/*
 * Copyright (c) 2018.
 * João Paulo Sena <joaopaulo761@gmail.com>
 *
 * This file is part of the UNES Open Source Project.
 *
 * UNES is licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.forcetower.unes.core.injection.module

import com.forcetower.unes.feature.home.HomeBottomFragment
import com.forcetower.unes.feature.messages.SagresMessagesFragment
import com.forcetower.unes.feature.messages.UnesMessagesFragment
import com.forcetower.unes.feature.schedule.ScheduleFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class HomeModule {
    @ContributesAndroidInjector
    abstract fun sagresMessageFragment(): SagresMessagesFragment
    @ContributesAndroidInjector
    abstract fun unesMessageFragment(): UnesMessagesFragment
    @ContributesAndroidInjector
    abstract fun homeBottomFragment(): HomeBottomFragment
    @ContributesAndroidInjector
    abstract fun scheduleFragment(): ScheduleFragment
}