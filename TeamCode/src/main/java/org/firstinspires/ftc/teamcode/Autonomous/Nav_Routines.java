package org.firstinspires.ftc.teamcode.Autonomous;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.teamcode.DbgLog;

import java.util.List;

abstract public class Nav_Routines extends LinearOpMode {
    DcMotor leftFront;
    DcMotor rightFront;
    DcMotor leftRear;
    DcMotor rightRear;
    DcMotor winchmotor;
    DcMotor mil1;
    DcMotor mil2;
    Servo mineralknockservo;
    ColorSensor mineralcs;
    ColorSensor mineralcs2;
    DistanceSensor mineralcsdistance;
    DistanceSensor mineralcsdistance2;
    DigitalChannel magneticlimitswitch;
    DistanceSensor leftdistancesensor;
    DistanceSensor frontdistancesensor;
    ModernRoboticsI2cRangeSensor rightdistancesensor;
    BNO055IMU imu;
    Orientation angles;

    double gs_previous_speed;
    double gs_previous_ticks_traveled;
    boolean gs_first_run = true;
    ElapsedTime gs_speed_timer = new ElapsedTime();

    private double wheel_encoder_ticks = 537.6;
    private double wheel_diameter = 3.75;  // size of wheels
    public double ticks_per_inch = wheel_encoder_ticks / (wheel_diameter * Math.PI);

    public double goforwardstopdetect = 2;
    public int ignorecrater = 150;

    int mil1startticks;
    int mil2startticks;
    int winchstartticks;

    double basealpha;
    double basealpha2;

    VuforiaLocalizer vuforia;

    public TFObjectDetector tfod;

    public void Nav_Init() {
        leftFront = hardwareMap.dcMotor.get("lf");
        rightFront = hardwareMap.dcMotor.get("rf");
        leftRear = hardwareMap.dcMotor.get("lr");
        rightRear = hardwareMap.dcMotor.get("rr");
        winchmotor = hardwareMap.dcMotor.get("wm");
        mil1 = hardwareMap.dcMotor.get("mil1");
        mil2 = hardwareMap.dcMotor.get("mil2");
        mineralknockservo = hardwareMap.servo.get("mks");
        mineralcs = hardwareMap.colorSensor.get("mcs");
        mineralcs2 = hardwareMap.colorSensor.get("mcs2");
        mineralcsdistance = hardwareMap.get(DistanceSensor.class, "mcs");
        mineralcsdistance2 = hardwareMap.get(DistanceSensor.class, "mcs2");
        frontdistancesensor = hardwareMap.get(DistanceSensor.class, "fds");
        leftdistancesensor = hardwareMap.get(DistanceSensor.class, "lds");
        rightdistancesensor = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "rds");
        magneticlimitswitch = hardwareMap.digitalChannel.get("mls");

        rightFront.setDirection(DcMotor.Direction.REVERSE);
        rightRear.setDirection(DcMotor.Direction.REVERSE);
        winchmotor.setDirection(DcMotor.Direction.REVERSE);

        mil1startticks = mil1.getCurrentPosition();
        mil2startticks = mil2.getCurrentPosition();
        winchstartticks = winchmotor.getCurrentPosition();

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftRear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightRear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        winchmotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        mil1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        mil2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftRear.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightRear.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        mil1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        mil2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        mil1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        mil2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        mineralknockservo.setPosition(1);

        BNO055IMU.Parameters IMUParameters = new BNO055IMU.Parameters();
        IMUParameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        IMUParameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        IMUParameters.calibrationDataFile = "BNO055IMUCalibration.json";

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(IMUParameters);

        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = "AWaEPBn/////AAAAGWa1VK57tkUipP01PNk9ghlRuxjK1Oh1pmbHuRnpaJI0vi57dpbnIkpee7J1pQ2RIivfEFrobqblxS3dKUjRo52NMJab6Me2Yhz7ejs5SDn4G5dheW5enRNWmRBsL1n+9ica/nVjG8xvGc1bOBRsIeZyL3EZ2tKSJ407BRgMwNOmaLPBle1jxqAE+eLSoYsz/FuC1GD8c4S3luDm9Utsy/dM1W4dw0hDJFc+lve9tBKGBX0ggj6lpo9GUrTC8t19YJg58jsIXO/DiF09a5jlrTeB2LK+GndUDEGyZA1mS3yAR6aIBeDYnFw+79mVFIkTPk8wv3HIQfzoggCu0AwWJBVUVjkDxJOWfzCGjaHylZlo";

        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.FRONT;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                    "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
            TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
            tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
            tfod.loadModelFromAsset("RoverRuckus.tflite", "Gold Mineral", "Silver Mineral");
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
        }

        if (tfod != null) {
            tfod.activate();
        }

        waitForStart();
    }

    public void turn_to_heading(double target_heading) {
        boolean go_right;
        double current_heading;
        double degrees_to_turn;
        double wheel_power;
        double prevheading = 0;
        ElapsedTime timeouttimer = new ElapsedTime();

        DbgLog.msg("10435 starting TURN_TO_HEADING");
        current_heading = currentheadingreading();
        degrees_to_turn = Math.abs(target_heading - current_heading);

        go_right = target_heading > current_heading;

        if (degrees_to_turn > 180) {
            go_right = !go_right;
            degrees_to_turn = 360 - degrees_to_turn;
        }

        timeouttimer.reset();
        prevheading = current_heading;
        while (degrees_to_turn > .5 && opModeIsActive() && timeouttimer.seconds() < 2) {

            wheel_power = (2 * Math.pow((degrees_to_turn + 13) / 30, 2) + 15) / 100;

            if (go_right) {
                wheel_power = -wheel_power;
            }

            rightFront.setPower(wheel_power);
            rightRear.setPower(wheel_power);
            leftFront.setPower(-wheel_power);
            leftRear.setPower(-wheel_power);

            current_heading = currentheadingreading();

            degrees_to_turn = Math.abs(target_heading - current_heading);       // Calculate how far is remaining to turn

            go_right = target_heading > current_heading;

            if (degrees_to_turn > 180) {
                go_right = !go_right;
                degrees_to_turn = 360 - degrees_to_turn;
            }

            if (Math.abs(current_heading - prevheading) > 1) {
                timeouttimer.reset();
                prevheading = current_heading;
            }

            DbgLog.msg("TURN_TO_HEADING" + " go right: " + go_right + " degrees to turn: " + degrees_to_turn + " wheel power: " + wheel_power + " current heading: " + current_heading + "Wheel power: " + Double.toString(wheel_power));
        }

        rightFront.setPower(0);
        rightRear.setPower(0);
        leftFront.setPower(0);
        leftRear.setPower(0);


        DbgLog.msg("10435 ending TURN_TO_HEADING" + Double.toString(target_heading) + "  Final heading:" + Double.toString(current_heading) + "  After set power 0:" + Double.toString(angles.firstAngle));

    } // end of turn_to_heading

    public boolean go_forward(double inches_to_travel, double heading, double speed, boolean runtimeoveride, boolean lookforcsalpha, int csalpha_min_inches) {

        DbgLog.msg("10435 starting GO_FORWARD inches:" + Double.toString(inches_to_travel) + " heading:" + Double.toString(heading) + " speed:" + Double.toString(speed));

        ElapsedTime log_timer = new ElapsedTime();

        double current_speed = .05;
        int ticks_to_travel;
        boolean destination_reached = false;
        boolean going_backwards = false;
        double speed_increase = .05;
        double actual_speed;
        double lagreduction = 2.125;
        double colorval = 0;
        int start_position_l_Front;
        int start_position_l_Rear;
        int start_position_r_Front;
        int start_position_r_Rear;
        int previous_ticks_traveled_L = 0;
        int ticks_traveled_l_Front;
        int ticks_traveled_l_Rear;
        int ticks_traveled_r_Front;
        int ticks_traveled_r_Rear;
        int lowest_ticks_traveled_l = 0;
        int lowest_ticks_traveled_r = 0;
        int lowest_ticks_traveled = 0;
        int highest_ticks_traveled_l;
        int highest_ticks_traveled_r;
        int highest_ticks_traveled = 0;
        double remaining_inches;
        double previous_log_timer = 0;
        double power_adjustment;
        boolean alphadetected = false;
        boolean linedetected = false;

        ElapsedTime timeouttimer = new ElapsedTime();

        if (speed < 0) {
            inches_to_travel = inches_to_travel * 1.08;
            speed_increase = -speed_increase;
            current_speed = -current_speed;
            going_backwards = true;
        }

        inches_to_travel = inches_to_travel - lagreduction;

        ticks_to_travel = (int) (inches_to_travel * ticks_per_inch);

        start_position_l_Front = leftFront.getCurrentPosition();
        start_position_l_Rear = leftRear.getCurrentPosition();
        start_position_r_Front = rightFront.getCurrentPosition();
        start_position_r_Rear = rightRear.getCurrentPosition();

        log_timer.reset();
        timeouttimer.reset();

        gs_first_run = true;

        while (opModeIsActive() && !destination_reached && timeouttimer.seconds() < goforwardstopdetect && !alphadetected) {

            if (lookforcsalpha && lowest_ticks_traveled / ticks_per_inch > csalpha_min_inches) {
                alphadetected = mineralcsdistance.getDistance(DistanceUnit.INCH) > 0 || mineralcsdistance2.getDistance(DistanceUnit.INCH) > 0;
            }
/*
            if (lookforcsalpha && lowest_ticks_traveled / ticks_per_inch > csalpha_min_inches) {
                alphadetected = mineralcs.alpha() - basealpha >= 6 || mineralcs2.alpha() - basealpha2 >= 6;
            }
*/
            current_speed = current_speed + speed_increase;  // this is to slowly ramp up the speed so we don't slip
            if (Math.abs(current_speed) > Math.abs(speed)) {
                current_speed = speed;
            }

            power_adjustment = go_straight_adjustment(heading);

            rightFront.setPower(current_speed + power_adjustment);
            rightRear.setPower(current_speed + power_adjustment);
            leftFront.setPower(current_speed - power_adjustment);
            leftRear.setPower(current_speed - power_adjustment);

            ticks_traveled_l_Front = Math.abs(leftFront.getCurrentPosition() - start_position_l_Front);
            ticks_traveled_l_Rear = Math.abs(leftRear.getCurrentPosition() - start_position_l_Rear);
            ticks_traveled_r_Front = Math.abs(rightFront.getCurrentPosition() - start_position_r_Front);
            ticks_traveled_r_Rear = Math.abs(rightRear.getCurrentPosition() - start_position_r_Rear);

            // of the 4 wheels, determines lowest ticks traveled
            lowest_ticks_traveled_l = Math.min(ticks_traveled_l_Front, ticks_traveled_l_Rear);
            lowest_ticks_traveled_r = Math.min(ticks_traveled_r_Front, ticks_traveled_r_Rear);
            lowest_ticks_traveled = Math.min(lowest_ticks_traveled_l, lowest_ticks_traveled_r);

            // of the 4 wheels, determines highest ticks traveled
            highest_ticks_traveled_l = Math.max(ticks_traveled_l_Front, ticks_traveled_l_Rear);
            highest_ticks_traveled_r = Math.max(ticks_traveled_r_Front, ticks_traveled_r_Rear);
            highest_ticks_traveled = Math.max(highest_ticks_traveled_l, highest_ticks_traveled_r);

            actual_speed = getSpeed(lowest_ticks_traveled);

            if (actual_speed > 0.1) {  // if we're going less than this we aren't moving.
                timeouttimer.reset();
            }

            if (lowest_ticks_traveled_l != previous_ticks_traveled_L && log_timer.seconds() - previous_log_timer > .1) {
                DbgLog.msg("10435 GO_FORWARD ticks_traveled: L:" + Double.toString(lowest_ticks_traveled_l)
                        + " R:" + Double.toString(lowest_ticks_traveled_r) + " actual_speed:" + actual_speed + " current speed:" + current_speed + " speed:" + speed);
                previous_log_timer = log_timer.seconds();
                previous_ticks_traveled_L = lowest_ticks_traveled_l;
            }

            destination_reached = (lowest_ticks_traveled >= ticks_to_travel);

            remaining_inches = inches_to_travel - ((double) lowest_ticks_traveled / ticks_per_inch);

            if (remaining_inches <= actual_speed && Math.abs(speed) > .2) {
                speed = .2;
                if (going_backwards) {
                    speed = -speed;
                }
                DbgLog.msg("10435 GO_FORWARD slowing down: remaining_inches:" + Double.toString(remaining_inches)
                        + " lowest_ticks_traveled:" + Integer.toString(lowest_ticks_traveled));
            }

        }
        rightFront.setPower(0);
        rightRear.setPower(0);
        leftFront.setPower(0);
        leftRear.setPower(0);


        sleep(100);
        DbgLog.msg("10435 ending GO_FORWARD: opModeIsActive:" + Boolean.toString(opModeIsActive())
                + " distance traveled L:" + Double.toString((lowest_ticks_traveled_l / ticks_per_inch))
                + " distance traveled R:" + Double.toString((lowest_ticks_traveled_r / ticks_per_inch))
                + " destination_reached:" + Boolean.toString(destination_reached)
                + " timouttimer:" + Double.toString(timeouttimer.seconds())
                + " lowest ticks traveled:" + Integer.toString(lowest_ticks_traveled)
                + " highest ticks traveled:" + Integer.toString(highest_ticks_traveled));

        return alphadetected;

    } // end of go_forward

    public double go_sideways(double angledegrees, double heading, double power, double inches, boolean zoneinonmineral) {

        DbgLog.msg("10435 Starting go_sideways"
                + " angledegrees:" + Double.toString(angledegrees)
                + " heading:" + Double.toString(heading)
                + " power:" + Double.toString(power)
                + " maxtime:" + Double.toString(inches)
        );

        double stickpower = power;
        double angleradians;
        double leftfrontpower;
        double rightfrontpower;
        double leftrearpower;
        double rightrearpower;
        double turningpower = 0;
        boolean mineralclose = false;
        boolean destinationreached = false;
        final double ticksperinch = 47;
        int ticks_to_travel;
        int start_position_l_Front;
        int start_position_l_Rear;
        int start_position_r_Front;
        int start_position_r_Rear;
        int ticks_traveled_l_Front;
        int ticks_traveled_l_Rear;
        int ticks_traveled_r_Front;
        int ticks_traveled_r_Rear;
        int highest_ticks_traveled_l;
        int highest_ticks_traveled_r;
        int highest_ticks_traveled = 0;

        start_position_l_Front = leftFront.getCurrentPosition();
        start_position_l_Rear = leftRear.getCurrentPosition();
        start_position_r_Front = rightFront.getCurrentPosition();
        start_position_r_Rear = rightRear.getCurrentPosition();

        ticks_to_travel = (int)(inches * ticksperinch);

        // For the cos and sin calculations below in the mecanum power calcs, angleradians = 0 is straight to the right and 180 is straight to the left.
        // Negative numbers up to -180 are backward.  Postive numbers up to 180 are forward.
        // We subtract 90 from it then convert degrees to radians because *our* robot code thinks of 0 degrees as forward, 90 as right, 180 as backward, 270 as left.

        // This converts from *our* degrees to radians used by the mecanum power calcs.
        // Upper left quadrant (degrees > 270) is special because in that quadrant as our degrees goes up, radians goes down.
        if (angledegrees < 270) {
            angleradians = ((angledegrees - 90) * -1) * Math.PI / 180;
        } else {
            angleradians = (450 - angledegrees) * Math.PI / 180;
        }

        angleradians = angleradians - Math.PI / 4; //adjust by 45 degrees for the mecanum wheel calculations below

        while (opModeIsActive() && !mineralclose && !destinationreached) {

            if (zoneinonmineral) {
                mineralclose = mineralcs.alpha() - basealpha >= 50 || mineralcs2.alpha() - basealpha2 >= 50;
            }

            ticks_traveled_l_Front = Math.abs(leftFront.getCurrentPosition() - start_position_l_Front);
            ticks_traveled_l_Rear = Math.abs(leftRear.getCurrentPosition() - start_position_l_Rear);
            ticks_traveled_r_Front = Math.abs(rightFront.getCurrentPosition() - start_position_r_Front);
            ticks_traveled_r_Rear = Math.abs(rightRear.getCurrentPosition() - start_position_r_Rear);

            // of the 4 wheels, determines highest ticks traveled
            highest_ticks_traveled_l = Math.max(ticks_traveled_l_Front, ticks_traveled_l_Rear);
            highest_ticks_traveled_r = Math.max(ticks_traveled_r_Front, ticks_traveled_r_Rear);
            highest_ticks_traveled = Math.max(highest_ticks_traveled_l, highest_ticks_traveled_r);


            if (highest_ticks_traveled >= ticks_to_travel){
                destinationreached = true;
            }

            turningpower = -go_straight_adjustment(heading) * (power * 2);

            leftfrontpower = stickpower * Math.cos(angleradians) + turningpower;
            rightfrontpower = stickpower * Math.sin(angleradians) - turningpower ;
            leftrearpower = stickpower * Math.sin(angleradians) + turningpower ;
            rightrearpower = stickpower * Math.cos(angleradians) - turningpower;



            leftFront.setPower(leftfrontpower);
            rightFront.setPower(rightfrontpower);
            leftRear.setPower(leftrearpower);
            rightRear.setPower(rightrearpower);

            DbgLog.msg("10435 inchesreadfromwall:"
                    + " turningpower:" + Double.toString(turningpower)
                    + " leftfrontpower" + Double.toString(leftfrontpower)
                    + " rightfrontpower" + Double.toString(rightfrontpower)
                    + " leftrearpower" + Double.toString(leftrearpower)
                    + " rightrearpower" + Double.toString(rightrearpower)
            );
        }

        leftFront.setPower(0);
        rightFront.setPower(0);
        leftRear.setPower(0);
        rightRear.setPower(0);

        sleep(50);

        return highest_ticks_traveled / ticksperinch;
    }

    public boolean go_sideways_to_wall(double heading, double power, double walldistance, boolean useleft, boolean findwall) {
        double stickpower = power;
        double angleradians;
        double leftfrontpower;
        double rightfrontpower;
        double leftrearpower;
        double rightrearpower;
        double turningpower;
        double inchesreadfromwall;
        double angledegrees;
        boolean wallfound = false;

        if (useleft) {
            inchesreadfromwall = leftdistancesensor.getDistance(DistanceUnit.INCH);
        } else {
            inchesreadfromwall = rightdistancesensor.getDistance(DistanceUnit.INCH);
        }

        if (useleft && walldistance - inchesreadfromwall > 0 || !useleft && walldistance - inchesreadfromwall < 0) {
            angledegrees = 90;
        } else {
            angledegrees = 270;
        }

        DbgLog.msg("10435 Starting go_sideways"
                + " angledegrees:" + Double.toString(angledegrees)
                + " heading:" + Double.toString(heading)
                + " power:" + Double.toString(power)
        );

        // For the cos and sin calculations below in the mecanum power calcs, angleradians = 0 is straight to the right and 180 is straight to the left.
        // Negative numbers up to -180 are backward.  Postive numbers up to 180 are forward.
        // We subtract 90 from it then convert degrees to radians because *our* robot code thinks of 0 degrees as forward, 90 as right, 180 as backward, 270 as left.

        // This converts from *our* degrees to radians used by the mecanum power calcs.
        // Upper left quadrant (degrees > 270) is special because in that quadrant as our degrees goes up, radians goes down.
        if (angledegrees < 270) {
            angleradians = ((angledegrees - 90) * -1) * Math.PI / 180;
        } else {
            angleradians = (450 - angledegrees) * Math.PI / 180;
        }

        angleradians = angleradians - Math.PI / 4; //adjust by 45 degrees for the mecanum wheel calculations below

        while (opModeIsActive() && Math.abs(walldistance - inchesreadfromwall) > .5 && !wallfound) {

            if (findwall) {
                wallfound = checkfrontdistancesensor();
            }

            if (useleft) {
                inchesreadfromwall = leftdistancesensor.getDistance(DistanceUnit.INCH);
            } else {
                inchesreadfromwall = rightdistancesensor.rawUltrasonic() / 2.5;
            }

            turningpower = -go_straight_adjustment(heading) * (power * 2);

            leftfrontpower = stickpower * Math.cos(angleradians) + turningpower;
            rightfrontpower = stickpower * Math.sin(angleradians) - turningpower;
            leftrearpower = stickpower * Math.sin(angleradians) + turningpower;
            rightrearpower = stickpower * Math.cos(angleradians) - turningpower;

            leftFront.setPower(leftfrontpower);
            rightFront.setPower(rightfrontpower);
            leftRear.setPower(leftrearpower);
            rightRear.setPower(rightrearpower);


            DbgLog.msg("10435 inchesreadfromwall:"
                    + " turningpower:" + Double.toString(turningpower)
                    + " leftfrontpower" + Double.toString(leftfrontpower)
                    + " rightfrontpower" + Double.toString(rightfrontpower)
                    + " leftrearpower" + Double.toString(leftrearpower)
                    + " rightrearpower" + Double.toString(rightrearpower)
            );

        }

        leftFront.setPower(0);
        rightFront.setPower(0);
        leftRear.setPower(0);
        rightRear.setPower(0);

        sleep(50);

        return wallfound;
    }

    public void wallfollow(double inches_to_travel, double heading, double speed, double walldistance, boolean left, boolean gotocrater) {

        DbgLog.msg("10435 starting WALL FOLLOW inches:" + Double.toString(inches_to_travel) + " heading:" + Double.toString(heading) + " speed:" + Double.toString(speed));

        ElapsedTime log_timer = new ElapsedTime();

        double current_speed = .05;
        int ticks_to_travel;
        boolean destination_reached = false;
        boolean going_backwards = false;
        double speed_increase = .05;
        double actual_speed;
        double lagreduction = 2.125;
        int start_position_l_Front;
        int start_position_l_Rear;
        int start_position_r_Front;
        int start_position_r_Rear;
        int previous_ticks_traveled_L = 0;
        int ticks_traveled_l_Front;
        int ticks_traveled_l_Rear;
        int ticks_traveled_r_Front;
        int ticks_traveled_r_Rear;
        int lowest_ticks_traveled_l = 0;
        int lowest_ticks_traveled_r = 0;
        int lowest_ticks_traveled = 0;
        int highest_ticks_traveled_l;
        int highest_ticks_traveled_r;
        int highest_ticks_traveled = 0;
        double remaining_inches;
        double previous_log_timer = 0;
        double power_adjustment;
        double distance_off;
        boolean wallfound = false;

        ElapsedTime timeouttimer = new ElapsedTime();

        if (speed < 0) {
            inches_to_travel = inches_to_travel * 1.08;
            speed_increase = -speed_increase;
            current_speed = -current_speed;
            going_backwards = true;
        }

        inches_to_travel = inches_to_travel - lagreduction;

        ticks_to_travel = (int) (inches_to_travel * ticks_per_inch);

        start_position_l_Front = leftFront.getCurrentPosition();
        start_position_l_Rear = leftRear.getCurrentPosition();
        start_position_r_Front = rightFront.getCurrentPosition();
        start_position_r_Rear = rightRear.getCurrentPosition();

        log_timer.reset();
        timeouttimer.reset();

        gs_first_run = true;

        while (opModeIsActive() && !destination_reached && timeouttimer.seconds() < goforwardstopdetect && !wallfound) {

            if (!gotocrater) {
                wallfound = checkfrontdistancesensor();
            }

            if (left) {
                distance_off = leftdistancesensor.getDistance(DistanceUnit.INCH) - walldistance;
            } else {
                distance_off = rightdistancesensor.rawUltrasonic() / 2.5 - walldistance;
            }

            if (Math.abs(distance_off) >= 1.5 && gotocrater) {
                go_sideways_to_wall(heading, .25, walldistance, left, false);
            } else if (Math.abs(distance_off) >= 1.5) {
                wallfound = go_sideways_to_wall(heading, .25, walldistance, left, true);
            }

            current_speed = current_speed + speed_increase;  // this is to slowly ramp up the speed so we don't slip
            if (Math.abs(current_speed) > Math.abs(speed)) {
                current_speed = speed;
            }

            power_adjustment = go_straight_adjustment(heading);

            rightFront.setPower(current_speed + power_adjustment);
            rightRear.setPower(current_speed + power_adjustment);
            leftFront.setPower(current_speed - power_adjustment);
            leftRear.setPower(current_speed - power_adjustment);

            ticks_traveled_l_Front = Math.abs(leftFront.getCurrentPosition() - start_position_l_Front);
            ticks_traveled_l_Rear = Math.abs(leftRear.getCurrentPosition() - start_position_l_Rear);
            ticks_traveled_r_Front = Math.abs(rightFront.getCurrentPosition() - start_position_r_Front);
            ticks_traveled_r_Rear = Math.abs(rightRear.getCurrentPosition() - start_position_r_Rear);

            // of the 4 wheels, determines lowest ticks traveled
            lowest_ticks_traveled_l = Math.min(ticks_traveled_l_Front, ticks_traveled_l_Rear);
            lowest_ticks_traveled_r = Math.min(ticks_traveled_r_Front, ticks_traveled_r_Rear);
            lowest_ticks_traveled = Math.min(lowest_ticks_traveled_l, lowest_ticks_traveled_r);

            // of the 4 wheels, determines highest ticks traveled
            highest_ticks_traveled_l = Math.max(ticks_traveled_l_Front, ticks_traveled_l_Rear);
            highest_ticks_traveled_r = Math.max(ticks_traveled_r_Front, ticks_traveled_r_Rear);
            highest_ticks_traveled = Math.max(highest_ticks_traveled_l, highest_ticks_traveled_r);

            actual_speed = getSpeed(lowest_ticks_traveled);

            if (actual_speed > 0.1) {  // if we're going less than this we aren't moving.
                timeouttimer.reset();
            }

            if (lowest_ticks_traveled_l != previous_ticks_traveled_L && log_timer.seconds() - previous_log_timer > .1) {
                DbgLog.msg("10435 GO_FORWARD ticks_traveled: L:" + Double.toString(lowest_ticks_traveled_l)
                        + " R:" + Double.toString(lowest_ticks_traveled_r) + " actual_speed:" + actual_speed + " current speed:" + current_speed + " speed:" + speed);
                previous_log_timer = log_timer.seconds();
                previous_ticks_traveled_L = lowest_ticks_traveled_l;
            }

            destination_reached = (lowest_ticks_traveled >= ticks_to_travel);

            remaining_inches = inches_to_travel - ((double) lowest_ticks_traveled / ticks_per_inch);

            if (remaining_inches <= actual_speed && Math.abs(speed) > .2) {
                speed = .2;
                if (going_backwards) {
                    speed = -speed;
                }
                DbgLog.msg("10435 GO_FORWARD slowing down: remaining_inches:" + Double.toString(remaining_inches)
                        + " lowest_ticks_traveled:" + Integer.toString(lowest_ticks_traveled));
            }

        }
        rightFront.setPower(0);
        rightRear.setPower(0);
        leftFront.setPower(0);
        leftRear.setPower(0);


        sleep(100);
        DbgLog.msg("10435 ending GO_FORWARD: opModeIsActive:" + Boolean.toString(opModeIsActive())
                + " distance traveled L:" + Double.toString((lowest_ticks_traveled_l / ticks_per_inch))
                + " distance traveled R:" + Double.toString((lowest_ticks_traveled_r / ticks_per_inch))
                + " destination_reached:" + Boolean.toString(destination_reached)
                + " timouttimer:" + Double.toString(timeouttimer.seconds())
                + " lowest ticks traveled:" + Integer.toString(lowest_ticks_traveled)
                + " highest ticks traveled:" + Integer.toString(highest_ticks_traveled));
    }

    public boolean mineralknock() {
        boolean yellowfound = false;
        double colorval;

        if (mineralcs.alpha() > mineralcs2.alpha()) {
            colorval = (mineralcs.red() - mineralcs.blue());
        } else {
            colorval = (mineralcs2.red() - mineralcs2.blue());
        }
        if (colorval >= 10) {
            yellowfound = true;
            mineralknockservo.setPosition(.5);
            telemetry.addLine("ITS YELLOW");
            telemetry.update();
            sleep(1000);
            mineralknockservo.setPosition(1);
        } else {
            telemetry.addLine("ITS WHITE");
            telemetry.update();
            sleep(500);
        }
        return yellowfound;
    }

    public void winchdown() {
        int winchticks;

        winchticks = winchmotor.getCurrentPosition() - winchstartticks;
        while (winchticks > 200) {
            winchticks = winchmotor.getCurrentPosition() - winchstartticks;
            winchmotor.setPower(-1);
        }
        winchmotor.setPower(0);
    }

    public void winchup() {
        boolean magnetistouching = false;

        while (!magnetistouching && opModeIsActive()) {
            magnetistouching = !magneticlimitswitch.getState();
            if (!magnetistouching) {
                winchmotor.setPower(1);
            } else {
                winchmotor.setPower(0);
            }
        }
    }

    public void deploymarker() {
        int mil1ticks;
        boolean liftisout = false;

        while (!liftisout && opModeIsActive()) {
            mil1ticks = mil1startticks - mil1.getCurrentPosition();
            if (mil1ticks < 180) {
                mil1.setPower(-.4);
                mil2.setPower(-.4);
            } else {
                mil1.setPower(0);
                mil2.setPower(0);
                liftisout = true;
            }
            telemetry.addData("Mil1Ticks", mil1ticks);
            telemetry.update();
        }

        boolean liftisback = false;

        while (!liftisback && opModeIsActive()) {
            mil1ticks = mil1startticks - mil1.getCurrentPosition();
            if (mil1ticks > 30) {
                mil1.setPower(.4);
                mil2.setPower(.4);
            } else {
                mil1.setPower(0);
                mil2.setPower(0);
                liftisback = true;
            }
            telemetry.addData("Mil1Ticks", mil1ticks);
            telemetry.update();
        }

    }

    public double turn_to_heading_tfod(double target_heading) {
        boolean go_right;
        double current_heading;
        double degrees_to_turn;
        double wheel_power;
        double prevheading;
        double angleGoldMineral = -500;
        double goldMineralBottom = 0;

        DbgLog.msg("10435 starting TURN_TO_HEADING_tfod");
        current_heading = currentheadingreading();
        degrees_to_turn = Math.abs(target_heading - current_heading);

        go_right = target_heading > current_heading;

        if (degrees_to_turn > 180) {
            go_right = !go_right;
            degrees_to_turn = 360 - degrees_to_turn;
        }

        prevheading = current_heading;
        while (degrees_to_turn > .5 && opModeIsActive() && angleGoldMineral == -500) {
            if (tfod != null) {
                List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                if (updatedRecognitions != null) {
                    if (updatedRecognitions.size() > 0) {
                        goldMineralBottom = 0;
                        for (Recognition recognition : updatedRecognitions) {
                            if (recognition.getLabel().equals("Gold Mineral") && recognition.getBottom() > ignorecrater && recognition.getBottom() > goldMineralBottom) {
                                angleGoldMineral = current_heading + recognition.estimateAngleToObject(AngleUnit.DEGREES);
                                angleGoldMineral = (angleGoldMineral + 360)%360;  // make sure it's not negative or higher than 360
                                goldMineralBottom = recognition.getBottom();      // save the bottom edge of the closest mineral which is how many pixels the bottom of the mineral is from the top of the image - max 720
                            }
                        }
                    }
                }
            }

            wheel_power = (2 * Math.pow((degrees_to_turn + 13) / 30, 2) + 15) / 100;

            if (go_right) {
                wheel_power = -wheel_power;
            }

            rightFront.setPower(wheel_power);
            rightRear.setPower(wheel_power);
            leftFront.setPower(-wheel_power);
            leftRear.setPower(-wheel_power);

            current_heading = currentheadingreading();

            degrees_to_turn = Math.abs(target_heading - current_heading);       // Calculate how far is remaining to turn

            go_right = target_heading > current_heading;

            if (degrees_to_turn > 180) {
                go_right = !go_right;
                degrees_to_turn = 360 - degrees_to_turn;
            }

            if (Math.abs(current_heading - prevheading) > 1) {
                prevheading = current_heading;
            }

            DbgLog.msg("10435 TURN_TO_HEADING_tfod" + " go right: " + go_right + " degrees to turn: " + degrees_to_turn + " wheel power: " + wheel_power + " current heading: " + current_heading + "Wheel power: " + Double.toString(wheel_power));
        }

        rightFront.setPower(0);
        rightRear.setPower(0);
        leftFront.setPower(0);
        leftRear.setPower(0);

        if (angleGoldMineral !=500) {
            if (turn_to_gold_tfod()) {
                angleGoldMineral = currentheadingreading();
            }
        }

        DbgLog.msg("10435 ending TURN_TO_HEADING_tfod" + Double.toString(target_heading) + "  Final heading:" + Double.toString(current_heading) + "  After set power 0:" + Double.toString(angles.firstAngle));

        return angleGoldMineral;
    }

    public boolean turn_to_gold_tfod() {
        double current_heading;
        double goldMineralBottom = 0;
        double angleGoldMineral = -500;

        sleep(100);  // give time to make sure it's stopped

        DbgLog.msg("10435 starting TURN_TO_GOLD_tfod");
        current_heading = currentheadingreading();

        if (tfod != null) {
            List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
            if (updatedRecognitions != null) {
                if (updatedRecognitions.size() > 0) {
                    goldMineralBottom = 0;
                    telemetry.addData("# Object Detected", updatedRecognitions.size());
                    for (Recognition recognition : updatedRecognitions) {
                        if (recognition.getLabel().equals("Gold Mineral") && recognition.getBottom() > ignorecrater && recognition.getBottom() > goldMineralBottom) {
                            angleGoldMineral = current_heading + recognition.estimateAngleToObject(AngleUnit.DEGREES);
                            angleGoldMineral = (angleGoldMineral + 360) % 360;  // make sure it's not negative or higher than 360
                            goldMineralBottom = recognition.getBottom();      // save the bottom edge of the closest mineral which is how many pixels the bottom of the mineral is from the top of the image - max 720
                        }
                    }
                }
            }
        }

        if (angleGoldMineral != -500) {
            telemetry.addData("Current heading", current_heading);
            telemetry.addData("Target Heading", angleGoldMineral);
            telemetry.update();
            turn_to_heading(angleGoldMineral);
        }

        rightFront.setPower(0);
        rightRear.setPower(0);
        leftFront.setPower(0);
        leftRear.setPower(0);

        DbgLog.msg("10435 ending TURN_TO_GOLD tfod" + Double.toString(angleGoldMineral) + "  Final heading:" + Double.toString(current_heading) + "  After set power 0:" + Double.toString(angles.firstAngle));

        return (goldMineralBottom !=0);
    }

    public void go_sideways_tfod(double angledegrees, double heading, double power) {
        double stickpower = power;
        double angleradians;
        double leftfrontpower;
        double rightfrontpower;
        double leftrearpower;
        double rightrearpower;
        double turningpower = 0;
        boolean mineralclose = false;
        double goldMineralBottom = 0;

        // For the cos and sin calculations below in the mecanum power calcs, angleradians = 0 is straight to the right and 180 is straight to the left.
        // Negative numbers up to -180 are backward.  Postive numbers up to 180 are forward.
        // We subtract 90 from it then convert degrees to radians because *our* robot code thinks of 0 degrees as forward, 90 as right, 180 as backward, 270 as left.

        // This converts from *our* degrees to radians used by the mecanum power calcs.
        // Upper left quadrant (degrees > 270) is special because in that quadrant as our degrees goes up, radians goes down.
        if (angledegrees < 270) {
            angleradians = ((angledegrees - 90) * -1) * Math.PI / 180;
        } else {
            angleradians = (450 - angledegrees) * Math.PI / 180;
        }

        angleradians = angleradians - Math.PI / 4; //adjust by 45 degrees for the mecanum wheel calculations below



        while (opModeIsActive() && !mineralclose) {
            if (tfod != null) {
                List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                if (updatedRecognitions != null) {
                    telemetry.addData("# Object Detected", updatedRecognitions.size());
                    if (updatedRecognitions.size() > 0) {
                        goldMineralBottom = 0;
                        for (Recognition recognition : updatedRecognitions) {
                            if (recognition.getLabel().equals("Gold Mineral") && recognition.getBottom() > ignorecrater && recognition.getBottom() > goldMineralBottom) {
                                goldMineralBottom = recognition.getBottom(); // save the bottom edge of the closest mineral which is how many pixels the bottom of the mineral is from the top of the image - max 720
                                mineralclose = (goldMineralBottom >= 640);
                            }
                        }
                    }
                }
                telemetry.addData("Gold Mineral Bottom", goldMineralBottom);
                telemetry.update();
            }

            turningpower = -go_straight_adjustment(heading) * (power * 2);

            leftfrontpower = stickpower * Math.cos(angleradians) + turningpower;
            rightfrontpower = stickpower * Math.sin(angleradians) - turningpower;
            leftrearpower = stickpower * Math.sin(angleradians) + turningpower;
            rightrearpower = stickpower * Math.cos(angleradians) - turningpower;

            leftFront.setPower(leftfrontpower);
            rightFront.setPower(rightfrontpower);
            leftRear.setPower(leftrearpower);
            rightRear.setPower(rightrearpower);


            DbgLog.msg("10435 inchesreadfromwall:"
                    + " turningpower:" + Double.toString(turningpower)
                    + " leftfrontpower" + Double.toString(leftfrontpower)
                    + " rightfrontpower" + Double.toString(rightfrontpower)
                    + " leftrearpower" + Double.toString(leftrearpower)
                    + " rightrearpower" + Double.toString(rightrearpower)
            );
        }

        leftFront.setPower(0);
        rightFront.setPower(0);
        leftRear.setPower(0);
        rightRear.setPower(0);

        sleep(50);
    }

    private double getSpeed(double ticks_traveled) {
        double new_speed;

        if (gs_first_run) {
            gs_previous_ticks_traveled = ticks_traveled;
            gs_speed_timer.reset();
            gs_previous_speed = 1;
            gs_first_run = false;
        }

        if (gs_speed_timer.seconds() >= .1) {
            new_speed = (ticks_traveled - gs_previous_ticks_traveled) / 46.5;  // At max speed we travel about 4800 ticks in a second so this give a range of 0 - 10 for speed
            gs_speed_timer.reset();
            gs_previous_speed = new_speed;
            gs_previous_ticks_traveled = ticks_traveled;
            DbgLog.msg("10435 getspeed:" + Double.toString(new_speed));
        } else {
            new_speed = gs_previous_speed;
        }

        return new_speed;
    }

    private boolean checkfrontdistancesensor() {
        boolean wallfound = false;

        if (frontdistancesensor.getDistance(DistanceUnit.INCH) <= 24) {
            wallfound = true;
        }

        return wallfound;
    }

    private double go_straight_adjustment(double target_heading) {

        //  This function outputs power_adjustment that should be added to right wheel and subtracted from left wheel

        double gs_adjustment;
        double current_heading;
        double degrees_off;
        boolean go_right;

        current_heading = currentheadingreading();

        DbgLog.msg("10435 starting go_straight_adjustment heading:" + Double.toString(target_heading) + " current heading:" + current_heading);

        go_right = target_heading > current_heading;
        degrees_off = Math.abs(target_heading - current_heading);

        if (degrees_off > 180) {
            go_right = !go_right;
            degrees_off = 360 - degrees_off;
        }

        if (degrees_off < .3) {
            gs_adjustment = 0;
        } else {
            gs_adjustment = (Math.pow((degrees_off + 2) / 5, 2) + 2) / 100;
        }

        if (go_right) {
            gs_adjustment = -gs_adjustment;
        }

        DbgLog.msg("10435 go_straight_adjustment adjustment:" + Double.toString(gs_adjustment));

        return gs_adjustment;

    } // end of go_straight_adjustment

    private double currentheadingreading() {
        double current_heading;
        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        current_heading = angles.firstAngle;

        if (current_heading < 0) {
            current_heading = -current_heading;
        } else {
            current_heading = 360 - current_heading;
        }

        current_heading = shiftheading(current_heading);

        return current_heading;
    }

    private double shiftheading(double heading) {
        double shiftvalue = 3;
        heading = heading + shiftvalue;

        if (heading >= 360) {
            heading = heading - 360;
        } else if (heading < 0) {
            heading = heading + 360;
        }
        return heading;
    }
}

