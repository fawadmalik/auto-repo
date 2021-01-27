pipeline {
  agent any
  stages {
    stage('Stage1') {
      steps {
        echo 'This is the $BUILD_NUMBER of the job called $DEMO'
        sh 'echo "This is Build $BUILD_NUMBER of demo $DEMO"'
      }
    }

  }
  environment {
    DEMO = '1'
  }
}