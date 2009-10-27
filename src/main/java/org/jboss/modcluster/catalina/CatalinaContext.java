/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.modcluster.catalina;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpSessionListener;

import org.jboss.modcluster.Context;
import org.jboss.modcluster.Host;

/**
 * @author Paul Ferraro
 */
public class CatalinaContext implements Context
{
   private final org.apache.catalina.Context context;
   private final Host host;
   
   public CatalinaContext(org.apache.catalina.Context context, Host host)
   {
      this.context = context;
      this.host = host;
   }
   
   public CatalinaContext(org.apache.catalina.Context context)
   {
      this.context = context;
      this.host = new CatalinaHost((org.apache.catalina.Host) context.getParent());
   }
   
   public Host getHost()
   {
      return this.host;
   }

   public String getPath()
   {
      return this.context.getPath();
   }

   public boolean isStarted()
   {
      try
      {
         return this.context.isStarted();
      }
      catch (NoSuchMethodError e)
      {
         return true;
      }
   }
   
   public int getActiveSessionCount()
   {
      return this.context.getManager().getActiveSessions();
   }

   public void addSessionListener(final HttpSessionListener listener)
   {
      synchronized (this.context)
      {
         Object[] listeners = this.context.getApplicationLifecycleListeners();
         List<Object> listenerList = new ArrayList<Object>(listeners.length + 1);
         
         listenerList.add(listener);
         listenerList.addAll(Arrays.asList(listeners));
         
         this.context.setApplicationLifecycleListeners(listenerList.toArray());
      }
   }

   public void removeSessionListener(HttpSessionListener listener)
   {
      synchronized (this.context)
      {
         List<Object> listenerList = new ArrayList<Object>(Arrays.asList(this.context.getApplicationLifecycleListeners()));

         listenerList.remove(listener);
         
         this.context.setApplicationLifecycleListeners(listenerList.toArray());
      }
   }

   public String toString()
   {
      return this.context.getPath();
   }
}
