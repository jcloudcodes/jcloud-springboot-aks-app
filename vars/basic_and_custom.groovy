def call(String buildStatus = 'STARTED') {
 buildStatus = buildStatus ?: 'SUCCESS'

 def color

 if (buildStatus == 'SUCCESS') {
  color = '#47ec05'
 } else if (buildStatus == 'UNSTABLE') {
  color = '#d5ee0d'
 } else {
  color = '#ec2805'
 }

 def msg = "${buildStatus}: `${env.JOB_NAME}` #${env.BUILD_NUMBER}:\n${env.BUILD_URL}"

 slackSend(color: color, message: msg)
}
===============
[
  "type": "divider"
],
[
  "type": "section",
  "fields": [
    [
      "type": "mrkdwn",
      "text": "*Codes Analysis Name:*\n${sonarName}"
    ],
    [
      "type": "mrkdwn",
      "text": "*Sonar Port*\n9000"
    ]
  ],
  "accessory": [
    "type": "image",
    "image_url": "https://github.com/agunuworld4/slack-devops-emoji/blob/main/main/slack-emojis/java-sonar.png",
    "alt_text": "SonarQube Icon"
  ],
],

[
  "type": "section",
  "text": [
      "type": "mrkdwn",
      "text": "*Analysis logs: * `Scanning`"
    ],
  "accessory": [
    "type": "button",
    "text": [
      "type": "plain_text",
      "text": "SonarQube URL",
      "emoji": true
    ],
    "value": "click_me_123",
    "url": "${sonarIP}:9000",
    "action_id": "button-action"
  ]
],
====================Nexus
[
  "type": "divider"
],
[
  "type": "section",
  "fields": [
    [
      "type": "mrkdwn",
      "text": "*Package Repo Name:*\n${nexusName}"
    ],
    [
      "type": "mrkdwn",
      "text": "*Nexus Port*\n8081"
    ]
  ],
  "accessory": [
    "type": "image",
    "image_url": "https://raw.githubusercontent.com/agunuworld4/slack-devops-emoji/refs/heads/main/main/slack-emojis/nexus.png",
    "alt_text": "Nexus Icon"
  ],
],

[
  "type": "section",
  "text": [
      "type": "mrkdwn",
      "text": "*War File: * `Package`"
    ],
  "accessory": [
    "type": "button",
    "text": [
      "type": "plain_text",
      "text": "Nexus URL",
      "emoji": true
    ],
    "value": "click_me_123",
    "url": "${nexusIP}:8081",
    "action_id": "button-action"
  ]
],
===============Docker
[
  "type": "divider"
],
[
  "type": "section",
  "fields": [
    [
      "type": "mrkdwn",
      "text": "*Package Repo Name:*\n${dockerName}"
    ],
    [
      "type": "mrkdwn",
      "text": "*Nexus Port*\n8081"
    ]
  ],
  "accessory": [
    "type": "image",
    "image_url": "https://raw.githubusercontent.com/agunuworld4/slack-devops-emoji/refs/heads/main/main/slack-emojis/docker.png",
    "alt_text": "Docker Icon"
  ],
],

[
  "type": "section",
  "text": [
      "type": "mrkdwn",
      "text": "*Docker Package: * `Images`"
    ],
  "accessory": [
    "type": "button",
    "text": [
      "type": "plain_text",
      "text": "Docker URL",
      "emoji": true
    ],
    "value": "click_me_123",
    "url": "${dockerIP}:8081",
    "action_id": "button-action"
  ]
],
=================prometheus
[
  "type": "divider"
],
[
  "type": "section",
  "fields": [
    [
      "type": "mrkdwn",
      "text": "*prometheus language query:*\n${promeName}"
    ],
    [
      "type": "mrkdwn",
      "text": "*Prome Port*\n9899"
    ]
  ],
  "accessory": [
    "type": "image",
    "image_url": "https://raw.githubusercontent.com/agunuworld4/slack-devops-emoji/refs/heads/main/main/slack-emojis/Prometheus.png",
    "alt_text": "prometheus Icon"
  ],
],

[
  "type": "section",
  "text": [
      "type": "mrkdwn",
      "text": "*prometheus Logs: * `logs`"
    ],
  "accessory": [
    "type": "button",
    "text": [
      "type": "plain_text",
      "text": "prometheus URL",
      "emoji": true
    ],
    "value": "click_me_123",
    "url": "${PromeLink}",
    "action_id": "button-action"
  ]
],
============alertmanager
[
  "type": "divider"
],
[
  "type": "section",
  "fields": [
    [
      "type": "mrkdwn",
      "text": "*Alert Manager:*\n${alertName}"
    ],
    [
      "type": "mrkdwn",
      "text": "*Alert Port*\n9899"
    ]
  ],
  "accessory": [
    "type": "image",
    "image_url": "https://raw.githubusercontent.com/agunuworld4/slack-devops-emoji/refs/heads/main/main/slack-emojis/alertmanger.png",
    "alt_text": "Alert Icon"
  ],
],

[
  "type": "section",
  "text": [
      "type": "mrkdwn",
      "text": "*Alert Trigger: * `Alert`"
    ],
  "accessory": [
    "type": "button",
    "text": [
      "type": "plain_text",
      "text": "alertmanger URL",
      "emoji": true
    ],
    "value": "click_me_123",
    "url": "${alartLink}",
    "action_id": "button-action"
  ]
],
=============Grafana
[
  "type": "divider"
],
[
  "type": "section",
  "fields": [
    [
      "type": "mrkdwn",
      "text": "*Grafana Dashboard Name:*\n${promeName}"
    ],
    [
      "type": "mrkdwn",
      "text": "*Grafana Port*\n9899"
    ]
  ],
  "accessory": [
    "type": "image",
    "image_url": "https://raw.githubusercontent.com/agunuworld4/slack-devops-emoji/refs/heads/main/main/slack-emojis/grafana.png",
    "alt_text": "Grafana Icon"
  ],
],

[
  "type": "section",
  "text": [
      "type": "mrkdwn",
      "text": "*Grafana Dashboard: * `visualization`"
    ],
  "accessory": [
    "type": "button",
    "text": [
      "type": "plain_text",
      "text": " URL",
      "emoji": true
    ],
    "value": "click_me_123",
    "url": "${grafanaURL}",
    "action_id": "button-action"
  ]
],
