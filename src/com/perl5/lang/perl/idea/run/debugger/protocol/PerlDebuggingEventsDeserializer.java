/*
 * Copyright 2016 Alexandr Evstigneev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.perl5.lang.perl.idea.run.debugger.protocol;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.intellij.openapi.util.text.StringUtil;

import java.lang.reflect.Type;

/**
 * Created by hurricup on 07.05.2016.
 */
public class PerlDebuggingEventsDeserializer implements JsonDeserializer<PerlDebuggingEvent>
{
	@Override
	public PerlDebuggingEvent deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
	{
		String event = jsonElement.getAsJsonObject().getAsJsonPrimitive("event").getAsString();

		PerlDebuggingEvent eventObject = null;

		if (StringUtil.isNotEmpty(event))
		{
			if (StringUtil.equals(event, "STOP"))
			{
				PerlDebuggingEventStop stopEvent = new PerlDebuggingEventStop();

				stopEvent.setFrames(
						(PerlDebuggingStackFrame[]) jsonDeserializationContext.deserialize(
								jsonElement.getAsJsonObject().getAsJsonArray("data"), PerlDebuggingStackFrame[].class
						));

				eventObject = stopEvent;
			}
			else if (StringUtil.equals(event, "BREAKPOINT_SET"))
			{
				eventObject = jsonDeserializationContext.deserialize(
						jsonElement.getAsJsonObject().getAsJsonObject("data"), PerlDebuggingEventBreakpointSet.class);
			}
			else if (StringUtil.equals(event, "BREAKPOINT_DENIED"))
			{
				eventObject = jsonDeserializationContext.deserialize(
						jsonElement.getAsJsonObject().getAsJsonObject("data"), PerlDebuggingEventBreakpointDenied.class);
			}
			else
			{
				System.err.println("Unhandled event in request: " + jsonElement.getAsString());
			}
		}
		else
		{
			System.err.println("Empty event in request: " + jsonElement.getAsString());
		}

		return eventObject;
	}

}
