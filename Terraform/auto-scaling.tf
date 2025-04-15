# Auto Scaling Group
resource "aws_autoscaling_group" "app_asg" {
  name                = "csye6225-asg-${var.profile}"
  min_size            = 3
  max_size            = 5
  desired_capacity    = 3
  default_cooldown    = 60
  vpc_zone_identifier = aws_subnet.public_subnet.*.id

  # Use launch template
  launch_template {
    id      = aws_launch_template.app_launch_template.id
    version = "$Latest"
  }

  # Attach to load balancer target group
  target_group_arns = [aws_lb_target_group.app_target_group.arn]

  # Health check settings
  health_check_type         = "ELB" # Use the load balancer's health check
  health_check_grace_period = 300

  # ASG tags that propagate to instances
  tag {
    key                 = "Name"
    value               = "${var.ec2_instance_name}-${var.profile}"
    propagate_at_launch = true
  }

  dynamic "tag" {
    for_each = var.tags
    content {
      key                 = tag.key
      value               = tag.value
      propagate_at_launch = true
    }
  }
}

# Scale Up Policy
resource "aws_autoscaling_policy" "scale_up_policy" {
  name                   = "scale-up-policy"
  autoscaling_group_name = aws_autoscaling_group.app_asg.name
  adjustment_type        = "ChangeInCapacity"
  scaling_adjustment     = 1
  cooldown               = 60
}

# Scale Down Policy
resource "aws_autoscaling_policy" "scale_down_policy" {
  name                   = "scale-down-policy"
  autoscaling_group_name = aws_autoscaling_group.app_asg.name
  adjustment_type        = "ChangeInCapacity"
  scaling_adjustment     = -1
  cooldown               = 60
}

# CloudWatch Alarm for High CPU
resource "aws_cloudwatch_metric_alarm" "high_cpu_alarm" {
  alarm_name          = "high-cpu-alarm-${var.profile}"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Average"
  threshold           = 8 # 5% CPU utilization

  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.app_asg.name
  }

  alarm_description = "Scale up when CPU exceeds 5%"
  alarm_actions     = [aws_autoscaling_policy.scale_up_policy.arn]
}

# CloudWatch Alarm for Low CPU
resource "aws_cloudwatch_metric_alarm" "low_cpu_alarm" {
  alarm_name          = "low-cpu-alarm-${var.profile}"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Average"
  threshold           = 7.5 # 3% CPU utilization

  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.app_asg.name
  }

  alarm_description = "Scale down when CPU is below 3%"
  alarm_actions     = [aws_autoscaling_policy.scale_down_policy.arn]
}