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

package com.perl5.lang.perl.idea.run.debugger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.containers.ByteArrayList;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.perl5.lang.perl.idea.run.debugger.breakpoints.PerlLineBreakPointDescriptor;
import com.perl5.lang.perl.idea.run.debugger.breakpoints.PerlLineBreakpointProperties;
import com.perl5.lang.perl.idea.run.debugger.breakpoints.PerlLineBreakpointType;
import com.perl5.lang.perl.idea.run.debugger.protocol.PerlDebuggingEvent;
import com.perl5.lang.perl.idea.run.debugger.protocol.PerlDebuggingEventsDeserializer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by hurricup on 04.05.2016.
 */
public class PerlDebugThread extends Thread
{
	private final Gson myGson;
	private XDebugSession mySession;
	private Socket mySocket;
	private OutputStream myOutputStream;
	private InputStream myInputStream;
	private Semaphore myResponseSemaphore = new Semaphore();
	private boolean myWaitForResponse = false;
	private byte[] myResponseBuffer;
	private boolean myStop = false;

	public PerlDebugThread(XDebugSession session)
	{
		super("PerlDebugThread");
		mySession = session;
		myGson = createGson();
	}

	@Override
	public void run()
	{
		try
		{
			mySocket = new Socket("localhost", 12345);
			myOutputStream = mySocket.getOutputStream();
			myInputStream = mySocket.getInputStream();

			ApplicationManager.getApplication().runReadAction(
					new Runnable()
					{
						@Override
						public void run()
						{
							Collection<? extends XLineBreakpoint<PerlLineBreakpointProperties>> breakpoints = XDebuggerManager.getInstance(mySession.getProject()).getBreakpointManager().getBreakpoints(PerlLineBreakpointType.class);

							List<PerlLineBreakPointDescriptor> descriptors = new ArrayList<PerlLineBreakPointDescriptor>();
							for (XLineBreakpoint<PerlLineBreakpointProperties> breakpoint : breakpoints)
							{
								PerlLineBreakPointDescriptor descriptor = PerlLineBreakPointDescriptor.createFromBreakpoint(breakpoint);
								if (descriptor != null)
								{
									descriptors.add(descriptor);
								}
							}

							sendString(new Gson().toJson(descriptors) + "\n");
						}
					}
			);

			ByteArrayList response = new ByteArrayList();

			while (!myStop)
			{
				response.clear();

				// reading bytes
				while (true)
				{
					byte newByte = (byte) myInputStream.read();
					if (newByte == '\n')
					{
						break;
					}
					else
					{
						response.add(newByte);
					}
				}

				if (myWaitForResponse)
				{
					myResponseBuffer = response.toNativeArray();
					myWaitForResponse = false;
					myResponseSemaphore.up();
				}
				else
				{
					processResponse(response);
				}
			}

		} catch (IOException e)
		{
		}
	}

	private void processResponse(ByteArrayList responseBytes)
	{
		String response = new String(responseBytes.toNativeArray(), CharsetToolkit.UTF8_CHARSET);
		PerlDebuggingEvent newEvent = myGson.fromJson(response, PerlDebuggingEvent.class);

		if (newEvent != null)
		{
			newEvent.doWork(mySession);
		}
	}

	public void sendString(String string)
	{
		if (mySocket == null)
			return;

		try
		{
			myOutputStream.write(string.getBytes());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Nullable
	public synchronized String sendStringAndGetResponse(String string)
	{
		if (mySocket == null)
			return null;

		myResponseSemaphore.down();
		myWaitForResponse = true;

		String response = null;
		try
		{
			myOutputStream.write(string.getBytes());
			myResponseSemaphore.waitFor();
			response = new String(myResponseBuffer, CharsetToolkit.UTF8_CHARSET);

		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return response;
	}

	public Socket getSocket()
	{
		return mySocket;
	}

	public void setStop()
	{
		myStop = true;
		try
		{
			if (myInputStream != null)
			{
				myInputStream.close();
				myInputStream = null;
			}
		} catch (IOException e)
		{
		}
		try
		{
			if (myOutputStream != null)
			{
				myOutputStream.close();
				myOutputStream = null;
			}
		} catch (IOException e)
		{
		}
		try
		{
			if (mySocket != null)
			{
				mySocket.close();
				mySocket = null;
			}
		} catch (IOException e)
		{
		}

	}

	protected Gson createGson()
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(PerlDebuggingEvent.class, new PerlDebuggingEventsDeserializer());
		return builder.create();
	}

}
