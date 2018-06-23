pipeline {
  agent any
  stages {
    stage('Build') {
      agent {
        docker {
          image 'gradle:4.8-jdk8'
        }

      }
      steps {
        sh '''./gradlew clean
./gradlew --max-workers 2 downloadDependencies
./gradlew --max-workers 2 --continue test'''
      }
    }
    stage('End') {
      steps {
        echo 'Finished!'
      }
    }
  }
  environment {
    token = 'MzQ2NzIwODEzMzQyMzkyMzMw.DII6pg.lgUCRRyk1ax1hF5-8qvIZuWiQiM'
    permanentChannel = '346719828784185377'
    modifyChannel = '347808531057213442'
    permanentMessage = '372217109985492993'
    reactionMessage = '372512766822449165'
    editMessage = '372516404345438208'
    permanentOverwrite = '84766711735136256'
    guild = '346719828784185375'
    permanentEmoji = '372524301267632129'
    trashCategory = '372526187668307968'
    permanentRole = '372528983138500619'
    member = '84745855399129088'
    permanentWebhook = '372884325278154752'
    inviteCode = 'ANzkjn5'
  }
}