/*
 * Developed by Luis Alcantara
 *
 * Copyright (C) 2016-2019 Prominic.NET, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 *
 * As a special exception, the copyright holders give permission to link the
 * code of portions of this program with the OpenSSL library under certain
 * conditions as described in each individual source file and distribute
 * linked combinations including the program with the OpenSSL library. You
 * must comply with the Server Side Public License in all respects for
 * all of the code used other than as permitted herein. If you modify file(s)
 * with this exception, you may extend this exception to your version of the
 * file(s), but you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version. If you delete this
 * exception statement from all source files in the program, then also delete
 * it in the license file.
 */

package switchboard.cloud

import grails.async.Promise
import grails.gorm.transactions.Transactional
import groovy.xml.MarkupBuilder
import security.Agent
import switchboard.cloud.CallRecord

import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

import static grails.async.Promises.task

@Transactional
class MailService {

    def ConfigLineService
    def groovyPageRenderer

    def sendMessage(props){
        String username = 'switchboard.mailer@gmail.com'
        String password = 'ndrzcfokdhcpnodh'

        Properties sessionProp = new Properties();
        sessionProp.put("mail.smtp.host", "smtp.gmail.com");
        sessionProp.put("mail.smtp.port", "587");
        sessionProp.put("mail.smtp.auth", "true");
        sessionProp.put("mail.smtp.starttls.enable", "true"); //TLS

        Session session = Session.getInstance(sessionProp,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        Message message = new MimeMessage(session);

        message.setContent(props.body, "text/html; charset=utf-8")
        message.setHeader("X-Mailer", "messagesend")
        message.setSentDate(new Date())
        message.setFrom(new InternetAddress('from@gmail.com'))

        try {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(props.to, false))
        } catch (AddressException e) {
            log.error "Exception occurred parsing To address(es) with value '${props.to}': ${e.message}"
        } catch (MessagingException e) {
            log.error "Exception occurred setting To field with value'${props.to}'. Check logs for password.\n\t${e.stackTrace}"
        }

        try {
            message.setSubject(props.subject)
        } catch (MessagingException e) {
            log.error "Exception occurred setting Subject field with value '${props.subject}':\n\t${e.message}"
        }

        try {
            Promise sendTask = task {
                Transport.send(message)
            }
            sendTask.onComplete {
                log.info "Email successfully sent to ${props.to}."
            }
            sendTask.onError {
                log.error "Error sending message:\n\t$it"
            }
        } catch (Exception e) {
            log.error "Exception occurred sending message:\n${e.stackTrace}"
        }
    }

    def sendCallRecordEmail(String uniqueId){
        CallRecord record = CallRecord.get(uniqueId)

        List<String> receipients = ConfigLineService.getConfigValue("mail", "call_record_report_receipients") ?:
                [Agent.get("101").username, Agent.get("106").username, Agent.get("103").username, Agent.get("112").username]

        Properties props = new Properties()

        props.putAll(to: receipients.join(" "),
                subject: "${record.direction.capitalize()} Call Notes for $record.customerName - $record.customerId",
                body: BuildCallReportBody(record))

        sendMessage(props)
    }

    def buildBody(String template, props){
        String body

        switch(template) {
            case ("New Agent"):
                body = BuildNewAgentBody(props.email, props.name, props.password)
                break
            case ("Voicemail Test"):
                body = BuildVoicemailTestBody()
                break
            case("Password Reset"):
                body = BuildPasswordResetBody(props.agent, props.password)
                break
            default:
                body = "No template found."
                break
        }

        body
    }

    private String BuildNewAgentBody(String email, String name, String password) {
        log.info "Generating email body for $name, with usename $email and password $password."

        """<h3 style='font-weight: normal'>Welcome to the SwitchBoard phone system <b>$name</b>,</h3>
            <h4 style='font-weight: normal'>Your username is <b>$email</b><br />
                <br />
                Your password is <b>$password</b><br />
                <br />
                It is recommended that you change this password ASAP to something you will remember and that only you know. 
                This can be done in the 'User Settings' menu, accessible via the side-pane in the inferface.<br />
                Please log in to the Switchboard instance and let the administrators know if there are any issues.<br />
                <br />
                Thanks,<br />
                <br />
                SwitchBoard by Prominic
            </h4>"""
    }

    private String BuildVoicemailTestBody(){
        MarkupBuilder builder = new MarkupBuilder()
        builder.div {
            h3(style: "font-weight: normal"){
                "This is a Test message meant to verfiy that the Phone Interfac"
            }
        }

    }

    private String BuildCallReportBody(CallRecord record){
        groovyPageRenderer.render(view: "/callRecord/email", model: [callRecord: record])
    }

    private String BuildPasswordResetBody(Agent agent, String password){
        log.info "Generating email body for $agent.name's password reset."

        groovyPageRenderer.render(view: "/admin/passwordReset", model: [agent: agent, password: password])
    }
}
