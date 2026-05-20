
def call(String buildStatus = 'STARTED') {
 buildStatus = buildStatus ?: 'SUCCESS'

 def color

 if (buildStatus == 'SUCCESS') {
  color = '#47ec05'
  emoji = ':ww:'
 } else if (buildStatus == 'UNSTABLE') {
  color = '#d5ee0d'
  emoji = ':deadpool:'
 } else {
  color = '#ec2805'
  emoji = ':hulk:'
 }

 attachments = [
    [
      "color": color,
      "blocks": [
        [
          "type": "header",
          "text": [
            "type": "plain_text",
            "text": "K8S Deployment - ${myApp} Pipeline  ${env.emoji}",
            "emoji": true
          ]
        ],
        [
          "type": "section",
          "fields": [
            [
              "type": "mrkdwn",
              "text": "*Job Name:*\n${env.JOB_NAME}"
            ],
            [
              "type": "mrkdwn",
              "text": "*Build Number:*\n${env.BUILD_NUMBER}"
            ]
          ],
          "accessory": [
            "type": "image",
            "image_url": "https://raw.githubusercontent.com/sidd-harth/kubernetes-devops-security/main/slack-emojis/jenkins.png",
            "alt_text": "Slack Icon"
          ]
        ],
        [
          "type": "section",
          "text": [
              "type": "mrkdwn",
              "text": "*Failed Stage Name: * `${env.failedStage}`"
            ],
          "accessory": [
            "type": "button",
            "text": [
              "type": "plain_text",
              "text": "Jenkins Build URL",
              "emoji": true
            ],
            "value": "click_me_123",
            "url": "${env.BUILD_URL}",
            "action_id": "button-action"
          ]
        ],
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
            "image_url": "https://raw.githubusercontent.com/agunuworld4/slack-devops-emoji/refs/heads/main/main/slack-emojis/sonar.png",
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
              "text": "*Image Port*\n8080"
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
            "url": "${dockerlink}",
            "action_id": "button-action"
          ]
        ],
        [
          "type": "divider"
        ],
        [
          "type": "section",
          "fields": [
            [
              "type": "mrkdwn",
              "text": "*Kubernetes Deployment Name:*\n${myApp}"
            ],
            [
              "type": "mrkdwn",
              "text": "*Node Port*\n32564"
            ]
          ],
          "accessory": [
            "type": "image",
            "image_url": "https://raw.githubusercontent.com/sidd-harth/kubernetes-devops-security/main/slack-emojis/k8s.png",
            "alt_text": "Kubernetes Icon"
          ],
        ],

        [
          "type": "section",
          "text": [
              "type": "mrkdwn",
              "text": "*Kubernetes Node: * `controlplane`"
            ],
          "accessory": [
            "type": "button",
            "text": [
              "type": "plain_text",
              "text": "Application URL",
              "emoji": true
            ],
            "value": "click_me_123",
            "url": "${webSite}",
            "action_id": "button-action"
          ]
        ],
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
        [
          "type": "divider"
        ],
        [
          "type": "section",
          "fields": [
            [
              "type": "mrkdwn",
              "text": "*Git Commit:*\n${GIT_COMMIT}"
            ],
            [
              "type": "mrkdwn",
              "text": "*GIT Previous Success Commit:*\n${GIT_PREVIOUS_SUCCESSFUL_COMMIT}"
            ]
          ],
          "accessory": [
            "type": "image",
            "image_url": "https://raw.githubusercontent.com/sidd-harth/kubernetes-devops-security/main/slack-emojis/github.png",
            "alt_text": "Github Icon"
          ]
        ],
        [
          "type": "section",
          "text": [
              "type": "mrkdwn",
              "text": "*Git Branch: * `${GIT_BRANCH}`"
            ],
          "accessory": [
            "type": "button",
            "text": [
              "type": "plain_text",
              "text": "Github Repo URL",
              "emoji": true
            ],
            "value": "click_me_123",
            "url": "${env.GIT_URL}",
            "action_id": "button-action"
          ]
        ]
      ]
    ]
  ]

 slackSend(iconEmoji: emoji, attachments: attachments)

}
