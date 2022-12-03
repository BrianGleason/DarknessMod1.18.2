package com.briangleason.darknessdraft;

public interface LightmapAccess {
	boolean darkness_isDirty();

	float darkness_prevFlicker();
}
