/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.console.tasks;

import java.io.Serializable;
import java.util.Date;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.pages.BasePage;
import org.apache.syncope.client.console.panels.TogglePanel;
import org.apache.syncope.client.console.rest.TaskRestClient;
import org.apache.syncope.client.console.wicket.markup.html.form.AjaxCheckBoxPanel;
import org.apache.syncope.client.console.wicket.markup.html.form.AjaxDateFieldPanel;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.SyncopeConstants;
import org.apache.syncope.common.lib.to.SchedTaskTO;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

public class StartAtTogglePanel extends TogglePanel<Serializable> {

    private static final long serialVersionUID = -3195479265440591519L;

    private SchedTaskTO taskTO;

    public StartAtTogglePanel(final WebMarkupContainer container) {
        super("startAt");

        final Form<?> form = new Form<>("startAtForm");
        addInnerObject(form);

        final Model<Date> startAtDateModel = new Model<Date>();

        final AjaxDateFieldPanel startAtDate = new AjaxDateFieldPanel(
                "startAtDate", "startAtDate", startAtDateModel, SyncopeConstants.DATE_PATTERNS[3]);

        startAtDate.setReadOnly(true).hideLabel();
        form.add(startAtDate);

        final AjaxCheckBoxPanel startAtCheck = new AjaxCheckBoxPanel(
                "startAtCheck", "startAtCheck", new Model<Boolean>(false), false);

        form.add(startAtCheck);

        startAtCheck.getField().add(new AjaxFormComponentUpdatingBehavior(Constants.ON_CHANGE) {

            private static final long serialVersionUID = -1107858522700306810L;

            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                target.add(startAtDate.setModelObject(null).setReadOnly(!startAtCheck.getModelObject()));
            }
        });

        form.add(new AjaxSubmitLink("startAt", form) {

            private static final long serialVersionUID = -7978723352517770644L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                try {
                    new TaskRestClient().startExecution(taskTO.getKey(), startAtDateModel.getObject());
                    info(getString(Constants.OPERATION_SUCCEEDED));
                    toggle(target, false);
                    target.add(container);
                } catch (SyncopeClientException e) {
                    error(getString(Constants.ERROR) + ": " + e.getMessage());
                    LOG.error("While running propagation task {}", taskTO.getKey(), e);
                }
                ((BasePage) getPage()).getNotificationPanel().refresh(target);
            }

            @Override
            protected void onError(final AjaxRequestTarget target, final Form<?> form) {
                ((BasePage) getPage()).getNotificationPanel().refresh(target);
            }

        });
    }

    public void setTaskTO(final AjaxRequestTarget target, final SchedTaskTO taskTO) {
        this.taskTO = taskTO;
        setHeader(target, taskTO.getName());
    }

}
