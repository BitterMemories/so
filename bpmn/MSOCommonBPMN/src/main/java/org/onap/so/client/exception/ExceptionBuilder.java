/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.client.exception;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExceptionBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionBuilder.class);

    public void buildAndThrowWorkflowException(BuildingBlockExecution execution, int errorCode, Exception exception) {
        String msg = "Exception in %s.%s ";
        try {
            logger.error("Exception occurred", exception);

            String errorVariable = "Error%s%s";

            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            for (StackTraceElement traceElement : trace) {
                if (!traceElement.getClassName().equals(this.getClass().getName())
                        && !traceElement.getClassName().equals(Thread.class.getName())) {
                    msg = String.format(msg, traceElement.getClassName(), traceElement.getMethodName());
                    String shortClassName =
                            traceElement.getClassName().substring(traceElement.getClassName().lastIndexOf(".") + 1);
                    errorVariable = String.format(errorVariable, shortClassName, traceElement.getMethodName());
                    break;
                }
            }

            logger.error("{} {} {} {} {}", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                    ErrorCode.UnknownError.getValue(), msg.toString());
            execution.setVariable(errorVariable, exception.getMessage());
        } catch (Exception ex) {
            // log trace, allow process to complete gracefully
            logger.error("Exception occurred", ex);
        }

        if (exception.getMessage() != null)
            msg = msg.concat(exception.getMessage());
        buildAndThrowWorkflowException(execution, errorCode, msg);
    }

    public void buildAndThrowWorkflowException(DelegateExecution execution, int errorCode, Exception exception) {
        String msg = "Exception in %s.%s ";
        try {
            logger.error("Exception occurred", exception);

            String errorVariable = "Error%s%s";

            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            for (StackTraceElement traceElement : trace) {
                if (!traceElement.getClassName().equals(this.getClass().getName())
                        && !traceElement.getClassName().equals(Thread.class.getName())) {
                    msg = String.format(msg, traceElement.getClassName(), traceElement.getMethodName());
                    String shortClassName =
                            traceElement.getClassName().substring(traceElement.getClassName().lastIndexOf(".") + 1);
                    errorVariable = String.format(errorVariable, shortClassName, traceElement.getMethodName());
                    break;
                }
            }
            logger.error("{} {} {} {} {}", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                    ErrorCode.UnknownError.getValue(), msg.toString());
            execution.setVariable(errorVariable, exception.getMessage());
        } catch (Exception ex) {
            // log trace, allow process to complete gracefully
            logger.error("Exception occurred", ex);
        }

        if (exception.getMessage() != null)
            msg = msg.concat(exception.getMessage());
        buildAndThrowWorkflowException(execution, errorCode, msg);
    }

    public void buildAndThrowWorkflowException(BuildingBlockExecution execution, int errorCode, String errorMessage) {
        if (execution instanceof DelegateExecutionImpl) {
            buildAndThrowWorkflowException(((DelegateExecutionImpl) execution).getDelegateExecution(), errorCode,
                    errorMessage);
        }
    }

    public void buildAndThrowWorkflowException(DelegateExecution execution, int errorCode, String errorMessage) {
        String processKey = getProcessKey(execution);
        logger.info("Building a WorkflowException for Subflow");

        WorkflowException exception = new WorkflowException(processKey, errorCode, errorMessage);
        execution.setVariable("WorkflowException", exception);
        execution.setVariable("WorkflowExceptionErrorMessage", errorMessage);
        logger.info("Outgoing WorkflowException is {}", exception);
        logger.info("Throwing MSOWorkflowException");
        throw new BpmnError("MSOWorkflowException");
    }

    public void buildAndThrowWorkflowException(DelegateExecution execution, String errorCode, String errorMessage) {
        execution.setVariable("WorkflowExceptionErrorMessage", errorMessage);
        throw new BpmnError(errorCode, errorMessage);
    }

    public String getProcessKey(DelegateExecution execution) {
        String testKey = (String) execution.getVariable("testProcessKey");
        if (testKey != null) {
            return testKey;
        }
        return execution.getProcessEngineServices().getRepositoryService()
                .getProcessDefinition(execution.getProcessDefinitionId()).getKey();
    }
}
