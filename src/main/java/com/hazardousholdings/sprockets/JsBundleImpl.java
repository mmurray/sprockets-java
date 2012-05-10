package com.hazardousholdings.sprockets;

import java.lang.annotation.Annotation;

import com.google.common.base.Preconditions;
import com.hazardousholdings.sprockets.annotations.JsBundle;

final class JsBundleImpl implements JsBundle {
	private final Asset asset;

	JsBundleImpl(Asset asset) {
		this.asset = Preconditions.checkNotNull(asset);
	}

	public String value() {
		return asset.getName().substring(1, asset.getName().lastIndexOf('.'));
	}

	// Algorithm is specified by java.lang.annotation.Annotation.
	@Override
	public int hashCode() {
		return ((127 * "value".hashCode()) ^ value().hashCode());
	}

	// Algorithm is specified by java.lang.annotation.Annotation.
	@Override
	public boolean equals(Object o) {
		if (o instanceof JsBundle) {
			JsBundle other = (JsBundle) o;
			return value().equals(other.value());
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format(
				"@%s(value=%s)",
				JsBundle.class.getName(), value());
	}

	public Class<? extends Annotation> annotationType() {
		return JsBundle.class;
	}
}
