package com.ibm.wala.cast.python.ml.cloud;

public class TestCloudFunction {

	public static void main(String[] args) {
			
		//String code1 = "1+2\nprint(123)";
			
		String code2 = "\"\"\" Convolutional Neural Network.\n" + 
				"\n" + 
				"Build and train a convolutional neural network with TensorFlow.\n" + 
				"This example is using the MNIST database of handwritten digits\n" + 
				"(http://yann.lecun.com/exdb/mnist/)\n" + 
				"\n" + 
				"This example is using TensorFlow layers API, see 'convolutional_network_raw'\n" + 
				"example for a raw implementation with variables.\n" + 
				"\n" + 
				"Author: Aymeric Damien\n" + 
				"Project: https://github.com/aymericdamien/TensorFlow-Examples/\n" + 
				"\"\"\"\n" + 
				"from __future__ import division, print_function, absolute_import\n" + 
				"\n" + 
				"# Import MNIST data\n" + 
				"from tensorflow.examples.tutorials.mnist import input_data\n" + 
				"mnist = input_data.read_data_sets(\"/tmp/data/\", one_hot=False)\n" + 
				"\n" + 
				"import tensorflow as tf\n" + 
				"\n" + 
				"# Training Parameters\n" + 
				"learning_rate = 0.001\n" + 
				"num_steps = 2000\n" + 
				"batch_size = 128\n" + 
				"\n" + 
				"# Network Parameters\n" + 
				"num_input = 784 # MNIST data input (img shape: 28*28)\n" + 
				"num_classes = 10 # MNIST total classes (0-9 digits)\n" + 
				"dropout = 0.75 # Dropout, probability to keep units\n" + 
				"\n" + 
				"# Create the neural network\n" + 
				"def conv_net(x_dict, n_classes, dropout, reuse, is_training):\n" + 
				"    # Define a scope for reusing the variables\n" + 
				"    with tf.variable_scope('ConvNet', reuse=reuse):\n" + 
				"        # TF Estimator input is a dict, in case of multiple inputs\n" + 
				"        x = x_dict['images']\n" + 
				"\n" + 
				"        bad_x = tf.reshape(x, shape=[-1, 11, 28, 1])\n" + 
				"\n" + 
				"        # MNIST data input is a 1-D vector of 784 features (28*28 pixels)\n" + 
				"        # Reshape to match picture format [Height x Width x Channel]\n" + 
				"        # Tensor input become 4-D: [Batch Size, Height, Width, Channel]\n" + 
				"        z = tf.reshape(x, shape=[-1, 28, 28, 1])\n" + 
				"\n" + 
				"        # Convolution Layer with 32 filters and a kernel size of 5\n" + 
				"        conv1 = tf.layers.conv2d(z, 32, 5, activation=tf.nn.relu)\n" + 
				"        # Max Pooling (down-sampling) with strides of 2 and kernel size of 2\n" + 
				"        conv1 = tf.layers.max_pooling2d(conv1, 2, 2)\n" + 
				"\n" + 
				"        # Convolution Layer with 64 filters and a kernel size of 3\n" + 
				"        conv2 = tf.layers.conv2d(conv1, 64, 3, activation=tf.nn.relu)\n" + 
				"        # Max Pooling (down-sampling) with strides of 2 and kernel size of 2\n" + 
				"        conv2 = tf.layers.max_pooling2d(conv2, 2, 2)\n" + 
				"\n" + 
				"        bad_conv1 = tf.layers.conv2d(x, 32, 5, activation=tf.nn.relu)\n" + 
				"\n" + 
				"        # Flatten the data to a 1-D vector for the fully connected layer\n" + 
				"        fc1 = tf.contrib.layers.flatten(conv2)\n" + 
				"\n" + 
				"        # Fully connected layer (in tf contrib folder for now)\n" + 
				"        fc1 = tf.layers.dense(fc1, 1024)\n" + 
				"        # Apply Dropout (if is_training is False, dropout is not applied)\n" + 
				"        fc1 = tf.layers.dropout(fc1, rate=dropout, training=is_training)\n" + 
				"\n" + 
				"        # Output layer, class prediction\n" + 
				"        out = tf.layers.dense(fc1, n_classes)\n" + 
				"\n" + 
				"    return out\n" + 
				"\n" + 
				"# Build the neural network\n" + 
				"# Define the model function (following TF Estimator Template)\n" + 
				"def model_fn(features, labels, mode):\n" + 
				"    # Because Dropout have different behavior at training and prediction time, we\n" + 
				"    # need to create 2 distinct computation graphs that still share the same weights.\n" + 
				"    logits_train = conv_net(features, num_classes, dropout, reuse=False,\n" + 
				"                            is_training=True)\n" + 
				"    logits_test = make_net(features, num_classes, dropout, reuse=True,\n" + 
				"                           is_training=False)\n" + 
				"\n" + 
				"    # Predictions\n" + 
				"    pred_classes = tf.argmax(logits_test, axis=1)\n" + 
				"    pred_probas = tf.nn.softmax(logits_test)\n" + 
				"\n" + 
				"    # If prediction mode, early return\n" + 
				"    if mode == tf.estimator.ModeKeys.PREDICT:\n" + 
				"        return tf.estimator.EstimatorSpec(mode, predictions=pred_classes)\n" + 
				"\n" + 
				"        # Define loss and optimizer\n" + 
				"    loss_op = tf.reduce_mean(tf.nn.sparse_softmax_cross_entropy_with_logits(\n" + 
				"        logits=logits_train, labels=tf.cast(labels, dtype=tf.int32)))\n" + 
				"    optimizer = tf.train.AdamOptimizer(learning_rate=learning_rate)\n" + 
				"    train_op = optimizer.minimize(loss_op,\n" + 
				"                                  global_step=tf.train.get_global_step())\n" + 
				"\n" + 
				"    # Evaluate the accuracy of the model\n" + 
				"    acc_op = tf.metrics.accuracy(labels=labels, predictions=pred_classes)\n" + 
				"\n" + 
				"    # TF Estimators requires to return a EstimatorSpec, that specify\n" + 
				"    # the different ops for training, evaluating, ...\n" + 
				"    estim_specs = tf.estimator.EstimatorSpec(\n" + 
				"        mode=mode,\n" + 
				"        predictions=pred_classes,\n" + 
				"        loss=loss_op,\n" + 
				"        train_op=train_op,\n" + 
				"        eval_metric_ops={'accuracy': acc_op})\n" + 
				"\n" + 
				"    return estim_specs\n" + 
				"\n" + 
				"make_net = conv_net\n" + 
				"\n" + 
				"# Build the Estimator\n" + 
				"model = tf.estimator.Estimator(model_fn)\n" + 
				"\n" + 
				"# Define the input function for training\n" + 
				"input_fn = tf.estimator.inputs.numpy_input_fn(\n" + 
				"    x={'images': mnist.train.images}, y=mnist.train.labels,\n" + 
				"    batch_size=batch_size, num_epochs=None, shuffle=True)\n" + 
				"# Train the Model\n" + 
				"model.train(input_fn, steps=num_steps)\n" + 
				"\n" + 
				"# Evaluate the Model\n" + 
				"# Define the input function for evaluating\n" + 
				"input_fn = tf.estimator.inputs.numpy_input_fn(\n" + 
				"    x={'images': mnist.test.images}, y=mnist.test.labels,\n" + 
				"    batch_size=batch_size, shuffle=False)\n" + 
				"# Use the Estimator 'evaluate' method\n" + 
				"e = model.evaluate(input_fn)\n" + 
				"\n" + 
				"print(\"Testing Accuracy:\", e['accuracy'])\n" + 
				"";
		
		String results = CloudFunction.analyze(code2);
		
		System.out.println(results);
			
	}

}