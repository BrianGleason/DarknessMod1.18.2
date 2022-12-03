/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package com.briangleason.darknessdraft.mixin;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import com.mojang.math.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.briangleason.darknessdraft.DarknessDraft;

@Mixin(DimensionSpecialEffects.NetherEffects.class)
public class MixinTheNetherDimension {
	private static double MIN = 0.03D;

	@Inject(method = "getBrightnessDependentFogColor", at = @At(value = "RETURN"), cancellable = true)
	private void onAdjustSkyColor(CallbackInfoReturnable<Vector3d> ci) {
		final double factor = DarknessDraft.darkNetherFog();

		if (factor != 1.0) {
			Vector3d result = ci.getReturnValue();
			result = new Vector3d(Math.max(MIN, result.x * factor), Math.max(MIN, result.y * factor), Math.max(MIN, result.z * factor));
			ci.setReturnValue(result);
		}
	}
}
